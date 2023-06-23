package me.meenagopal24.recyclerdemo;

public class ImageUploadResponse {
    String status , fileName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ImageUploadResponse(String status, String fileName) {
        this.status = status;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ImageUploadResponse{" +
                "status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
