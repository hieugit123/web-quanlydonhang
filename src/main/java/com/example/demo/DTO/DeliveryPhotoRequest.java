package com.example.demo.DTO;

import lombok.Data;

@Data
public class DeliveryPhotoRequest {
    private String photoUrl;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

