package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration request entity.
 * 
 * @author DevUser
 */
@Entity
@Table(name = "T_USER_REGISTRATION_REQUEST")
public class UserRegistrationRequest implements Loggable {
    /**
     * Registration request ID.
     */
    @Id
    @Column(name = "URR_ID_C", length = 36)
    private String id;
    
    /**
     * User's username.
     */
    @Column(name = "URR_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "URR_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Email address.
     */
    @Column(name = "URR_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Creation date.
     */
    @Column(name = "URR_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Registration status (PENDING, APPROVED, REJECTED).
     */
    @Column(name = "URR_STATUS_C", nullable = false, length = 20)
    private String status;
    
    /**
     * Process date.
     */
    @Column(name = "URR_PROCESSDATE_D")
    private Date processDate;
    
    /**
     * Admin who processed the request.
     */
    @Column(name = "URR_PROCESSEDBY_C", length = 36)
    private String processedBy;
    
    public String getId() {
        return id;
    }

    public UserRegistrationRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRegistrationRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserRegistrationRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRegistrationRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public UserRegistrationRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRegistrationRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public UserRegistrationRequest setProcessDate(Date processDate) {
        this.processDate = processDate;
        return this;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public UserRegistrationRequest setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }

    @Override
    public Date getDeleteDate() {
        return null; // Registration requests don't have delete date, they are approved or rejected
    }
}