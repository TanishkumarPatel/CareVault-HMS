package com.example.aiservice.dto;

public class chatResponse {
    private boolean isFinal;
    private String aiMessage;
    private String department;
    private String urgency;
    private String reason;
    private String triageId;

    public static chatResponse question(String aiMessage) {
        chatResponse r = new chatResponse();
        r.isFinal = false;
        r.aiMessage = aiMessage;
        return r;
    }

    public static chatResponse finalResult(String department, String urgency, String reason, String triageId) {
        chatResponse r = new chatResponse();
        r.isFinal = true;
        r.department = department;
        r.urgency = urgency;
        r.reason = reason;
        r.triageId = triageId;
        return r;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getAiMessage() {
        return aiMessage;
    }

    public String getDepartment() {
        return department;
    }

    public String getUrgency() {
        return urgency;
    }

    public String getReason() {
        return reason;
    }

    public String getTriageId() {
        return triageId;
    }
}
