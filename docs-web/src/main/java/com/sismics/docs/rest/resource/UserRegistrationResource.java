package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRegistrationDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * User registration request REST resources.
 * 
 * @author DevUser
 */
@Path("/user/registration")
public class UserRegistrationResource extends BaseResource {
    
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationResource.class);
    
    /**
     * Submit a new user registration request (accessible to guests).
     *
     * @param username Username
     * @param password Password
     * @param email Email
     * @return Response
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {
        
        // Validate input
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateEmail(email, "email");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        
        try {
            // Create the registration request
            UserRegistrationRequest request = new UserRegistrationRequest();
            request.setUsername(username);
            request.setPassword(password); // Will be hashed when creating the actual user
            request.setEmail(email);
            
            UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
            String id = userRegistrationDao.create(request);
            
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("id", id);
            return Response.ok().entity(response.toString()).build();
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "用户名已存在");
            } else if ("PendingRegistrationExists".equals(e.getMessage())) {
                throw new ClientException("PendingRegistrationExists", "申请已提交，请等待审核");
            }
            log.error("Error during registration", e);
            throw new ServerException("RegistrationError", "注册申请提交失败", e);
        }
    }
    
    /**
     * Returns all pending registration requests (admin only).
     *
     * @return Response
     */
    @GET
    @Path("/pending")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPendingRequests() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
        List<UserRegistrationRequest> pendingRequests = userRegistrationDao.findPendingRequests();
        
        JSONObject response = new JSONObject();
        JSONArray requests = new JSONArray();
        
        for (UserRegistrationRequest request : pendingRequests) {
            JSONObject requestJson = new JSONObject();
            requestJson.put("id", request.getId());
            requestJson.put("username", request.getUsername());
            requestJson.put("email", request.getEmail());
            requestJson.put("create_date", request.getCreateDate().getTime());
            requests.put(requestJson);
        }
        
        response.put("requests", requests);
        return Response.ok().entity(response.toString()).build();
    }
    
    /**
     * Approves a registration request (admin only).
     *
     * @param id Registration request ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveRequest(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get current admin ID
        String adminId = principal.getId();
        
        try {
            UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
            UserRegistrationRequest request = userRegistrationDao.approve(id, adminId);
            
            if (request == null) {
                throw new ClientException("NotFound", "申请不存在或已处理");
            }
            
            // Create the user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail());
            user.setRoleId(Constants.DEFAULT_USER_ROLE); // Default user role
            user.setStorageQuota(1000000000L); // 1GB quota by default
            user.setOnboarding(true);
            
            UserDao userDao = new UserDao();
            String userId = userDao.create(user, adminId);
            
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("user_id", userId);
            return Response.ok().entity(response.toString()).build();
        } catch (Exception e) {
            log.error("Error approving registration", e);
            throw new ServerException("ApprovalError", "审批失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Rejects a registration request (admin only).
     *
     * @param id Registration request ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectRequest(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get current admin ID
        String adminId = principal.getId();
        
        UserRegistrationDao userRegistrationDao = new UserRegistrationDao();
        UserRegistrationRequest request = userRegistrationDao.reject(id, adminId);
        
        if (request == null) {
            throw new ClientException("NotFound", "申请不存在或已处理");
        }
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response.toString()).build();
    }
}