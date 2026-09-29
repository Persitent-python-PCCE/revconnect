package com.revconnect.interaction.client.dto;

public class CreateNotificationRequest {
    private Long recipientId;
    private Long actorId;
    private String actorUsername;
    private String message;
    private String type;
    private Long referenceId;

    public CreateNotificationRequest() {}

    public CreateNotificationRequest(Long recipientId, Long actorId, String actorUsername,
                                     String message, String type, Long referenceId) {
        this.recipientId = recipientId;
        this.actorId = actorId;
        this.actorUsername = actorUsername;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
    }

    public Long getRecipientId() { return recipientId; }
    public Long getActorId() { return actorId; }
    public String getActorUsername() { return actorUsername; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Long getReferenceId() { return referenceId; }
}
