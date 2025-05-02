package service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.FileInputStream;
import java.io.InputStream;

public class DropboxService {

    private final DbxClientV2 client;

    public DropboxService(String accessToken) {
        // Initialize Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("pdf-uploader").build();
        this.client = new DbxClientV2(config, accessToken);
    }

    /**
     * Uploads a file to Dropbox.
     * @param localFilePath The path of the file to upload.
     * @param dropboxPath The destination path in Dropbox (e.g., "/pdfs/example.pdf").
     */
    public void uploadFile(String localFilePath, String dropboxPath) {
        try (InputStream inputStream = new FileInputStream(localFilePath)) {
            System.out.println("Uploading file to Dropbox: " + dropboxPath);
            FileMetadata metadata = client.files().uploadBuilder(dropboxPath)
                    .uploadAndFinish(inputStream);
            System.out.println("File uploaded successfully to Dropbox: " + metadata.getPathDisplay());
        } catch (Exception e) {
            System.err.println("Error uploading file to Dropbox: " + e.getMessage());
        }
    }

    /**
     * Retrieves the public URL of a file in Dropbox.
     * If a shared link already exists, it retrieves the existing link.
     * @param dropboxPath The path of the file in Dropbox (e.g., "/pdfs/example.pdf").
     * @return The public URL of the file.
     */
    public String getFileUrl(String dropboxPath) {
        try {
            System.out.println("Generating shared link for file: " + dropboxPath);
            SharedLinkMetadata sharedLinkMetadata = client.sharing().createSharedLinkWithSettings(dropboxPath);
            return sharedLinkMetadata.getUrl();
        } catch (CreateSharedLinkWithSettingsErrorException e) {
            if (e.errorValue.isSharedLinkAlreadyExists()) {
                try {
                    System.out.println("Shared link already exists. Retrieving existing link...");
                    ListSharedLinksResult linksResult = client.sharing().listSharedLinksBuilder()
                            .withPath(dropboxPath)
                            .withDirectOnly(true)
                            .start();
                    if (!linksResult.getLinks().isEmpty()) {
                        return linksResult.getLinks().get(0).getUrl();
                    } else {
                        System.err.println("No existing shared link found.");
                        return null;
                    }
                } catch (DbxException ex) {
                    System.err.println("Error retrieving existing shared link: " + ex.getMessage());
                    return null;
                }
            } else {
                System.err.println("Error creating shared link: " + e.getMessage());
                return null;
            }
        } catch (DbxException e) {
            System.err.println("Dropbox error: " + e.getMessage());
            return null;
        }
    }
}
