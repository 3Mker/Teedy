package com.sismics.docs.core.dao;

import com.google.common.base.Strings;
import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.docs.core.util.DirectoryUtil;
import jakarta.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User Registration Request DAO using JSON file storage.
 * 
 * @author DevUser
 */
public class UserRegistrationDao {
    
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationDao.class);
    
    /**
     * JSON file path.
     */
    private static final String JSON_FILE = DirectoryUtil.getDataDirectory().toString() + "/registration_requests.json";
    
    /**
     * File lock to prevent concurrent file access.
     */
    private static final ReentrantLock fileLock = new ReentrantLock();
    
    /**
     * Creates a new user registration request.
     * 
     * @param request User registration request
     * @return User registration request ID
     * @throws Exception e
     */
    public String create(UserRegistrationRequest request) throws Exception {
        // Create the UUID
        request.setId(UUID.randomUUID().toString());
        
        // Check for username uniqueness in users
        UserDao userDao = new UserDao();
        if (userDao.getActiveByUsername(request.getUsername()) != null) {
            throw new Exception("AlreadyExistingUsername");
        }
        
        fileLock.lock();
        try {
            // Load existing requests
            List<UserRegistrationRequest> requests = loadRequests();
            
            // Check for pending registration with same username
            for (UserRegistrationRequest existingRequest : requests) {
                if (existingRequest.getUsername().equals(request.getUsername()) && 
                    "PENDING".equals(existingRequest.getStatus())) {
                    throw new Exception("PendingRegistrationExists");
                }
            }
            
            // Create the request
            request.setCreateDate(new Date());
            request.setStatus("PENDING");
            
            // Add to list and save
            requests.add(request);
            saveRequests(requests);
            
            return request.getId();
        } finally {
            fileLock.unlock();
        }
    }
    
    /**
     * Gets all pending registration requests.
     * 
     * @return List of pending registration requests
     */
    public List<UserRegistrationRequest> findPendingRequests() {
        fileLock.lock();
        try {
            List<UserRegistrationRequest> allRequests = loadRequests();
            List<UserRegistrationRequest> pendingRequests = new ArrayList<>();
            
            for (UserRegistrationRequest request : allRequests) {
                if ("PENDING".equals(request.getStatus())) {
                    pendingRequests.add(request);
                }
            }
            
            return pendingRequests;
        } finally {
            fileLock.unlock();
        }
    }
    
    /**
     * Get a specific registration request by ID.
     * 
     * @param id Registration request ID
     * @return Registration request
     */
    public UserRegistrationRequest getById(String id) {
        fileLock.lock();
        try {
            List<UserRegistrationRequest> requests = loadRequests();
            
            for (UserRegistrationRequest request : requests) {
                if (request.getId().equals(id)) {
                    return request;
                }
            }
            
            return null;
        } finally {
            fileLock.unlock();
        }
    }
    
    /**
     * Approve a registration request.
     * 
     * @param id Registration request ID
     * @param adminId Admin user ID
     * @return Updated registration request
     */
    public UserRegistrationRequest approve(String id, String adminId) {
        fileLock.lock();
        try {
            List<UserRegistrationRequest> requests = loadRequests();
            UserRegistrationRequest updatedRequest = null;
            
            for (UserRegistrationRequest request : requests) {
                if (request.getId().equals(id) && "PENDING".equals(request.getStatus())) {
                    request.setStatus("APPROVED");
                    request.setProcessDate(new Date());
                    request.setProcessedBy(adminId);
                    updatedRequest = request;
                    break;
                }
            }
            
            if (updatedRequest != null) {
                saveRequests(requests);
            }
            
            return updatedRequest;
        } finally {
            fileLock.unlock();
        }
    }
    
    /**
     * Reject a registration request.
     * 
     * @param id Registration request ID
     * @param adminId Admin user ID
     * @return Updated registration request
     */
    public UserRegistrationRequest reject(String id, String adminId) {
        fileLock.lock();
        try {
            List<UserRegistrationRequest> requests = loadRequests();
            UserRegistrationRequest updatedRequest = null;
            
            for (UserRegistrationRequest request : requests) {
                if (request.getId().equals(id) && "PENDING".equals(request.getStatus())) {
                    request.setStatus("REJECTED");
                    request.setProcessDate(new Date());
                    request.setProcessedBy(adminId);
                    updatedRequest = request;
                    break;
                }
            }
            
            if (updatedRequest != null) {
                saveRequests(requests);
            }
            
            return updatedRequest;
        } finally {
            fileLock.unlock();
        }
    }
    
    /**
     * Load all registration requests from JSON file.
     * 
     * @return List of registration requests
     */
    private List<UserRegistrationRequest> loadRequests() {
        List<UserRegistrationRequest> requests = new ArrayList<>();
        File file = new File(JSON_FILE);
        
        if (!file.exists()) {
            return requests;
        }
        
        try (FileReader fileReader = new FileReader(file);
             JsonReader jsonReader = Json.createReader(fileReader)) {
            
            JsonArray requestsArray = jsonReader.readArray();
            
            for (int i = 0; i < requestsArray.size(); i++) {
                JsonObject requestObj = requestsArray.getJsonObject(i);
                UserRegistrationRequest request = new UserRegistrationRequest();
                
                request.setId(requestObj.getString("id"));
                request.setUsername(requestObj.getString("username"));
                request.setPassword(requestObj.getString("password"));
                request.setEmail(requestObj.getString("email"));
                request.setStatus(requestObj.getString("status"));
                
                if (requestObj.containsKey("createDate")) {
                    request.setCreateDate(new Date(requestObj.getJsonNumber("createDate").longValue()));
                }
                
                if (requestObj.containsKey("processDate") && !requestObj.isNull("processDate")) {
                    request.setProcessDate(new Date(requestObj.getJsonNumber("processDate").longValue()));
                }
                
                if (requestObj.containsKey("processedBy") && !requestObj.isNull("processedBy")) {
                    request.setProcessedBy(requestObj.getString("processedBy"));
                }
                
                requests.add(request);
            }
            
        } catch (IOException e) {
            log.error("Error loading registration requests", e);
        }
        
        return requests;
    }
    
    /**
     * Save all registration requests to JSON file.
     * 
     * @param requests List of registration requests
     */
    private void saveRequests(List<UserRegistrationRequest> requests) {
        try (FileWriter fileWriter = new FileWriter(new File(JSON_FILE));
             JsonWriter jsonWriter = Json.createWriter(fileWriter)) {
            
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            
            for (UserRegistrationRequest request : requests) {
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
                        .add("id", request.getId())
                        .add("username", request.getUsername())
                        .add("password", request.getPassword())
                        .add("email", request.getEmail())
                        .add("status", request.getStatus())
                        .add("createDate", request.getCreateDate().getTime());
                
                if (request.getProcessDate() != null) {
                    objectBuilder.add("processDate", request.getProcessDate().getTime());
                } else {
                    objectBuilder.addNull("processDate");
                }
                
                if (!Strings.isNullOrEmpty(request.getProcessedBy())) {
                    objectBuilder.add("processedBy", request.getProcessedBy());
                } else {
                    objectBuilder.addNull("processedBy");
                }
                
                arrayBuilder.add(objectBuilder);
            }
            
            jsonWriter.writeArray(arrayBuilder.build());
            
        } catch (IOException e) {
            log.error("Error saving registration requests", e);
        }
    }
}