/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DiplomskiRad;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/* class to demonstarte use of Drive files list API */
public class GoogleDriveOperations {

    private static final String APPLICATION_NAME = "DiplomskiRad";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/cred.json";

    private NetHttpTransport HTTP_TRANSPORT;
    private Drive service;
    private File myFile;
    private boolean logged = false;
    // client secret
    private String password;
    private String salt;
    private boolean goodPassword;

    public GoogleDriveOperations() {
        try {
            System.out.println("GoogleDriveOperations -> Konstruktor -> BEGIN");
            this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            this.myFile = this.searchFile();// fajl sa GoogleDrive-a
            String clientPtivate = "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";

            if (this.myFile == null) {
                // pravimo tri fajla: jedan za drajv, dva za lokalno
                this.myFile = this.makeNewFile();
                this.salt = this.myFile.getId();
                this.logged = false;
            } else {
                this.salt = this.myFile.getId();
                this.downloadFile();
                //fajl sa GoogleDrive-a upisuje u example.bin
                this.logged = true;
            }
            // sad smo sigurni da imamo fajl ovde i na klaudu

            System.out.println("GoogleDriveOperations -> Konstruktor -> password: " + password + ", salt: " + salt);

            System.out.println("GoogleDriveOperations -> Konstruktor -> myFile.ID = " + myFile.getId());
            //this.printAbout(service);
            System.out.println("GoogleDriveOperations -> Konstruktor -> END");
        } catch (GeneralSecurityException | IOException ex) {
            Logger.getLogger(GoogleDriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printAbout(Drive service) {
        try {
            com.google.api.services.drive.model.About about = service.about().get().execute();
            //System.out.println("Current user name: " + about.getUser().getDisplayName());
            System.out.println("email: " + about.getUser().toString());
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }

    // obavezno je na kraju da se pozove
    public void logout() {
        //java.io.File file1 = new java.io.File("..\\app\\tokens\\StoredCredential");
        java.io.File file2 = new java.io.File("../app/src/main/resources/example.json");
        java.io.File file3 = new java.io.File("../app/src/main/resources/example.bin");

        boolean file1d = true;//file1.delete();
        boolean file2d = file2.delete();
        boolean file3d = file3.delete();

        if (file1d && file2d && file3d) {
            System.out.println("GoogleDriveOperations -> logout -> Files deleted successfully");
        } else {
            System.out.println("GoogleDriveOperations -> logout -> Failed to delete the files");
        }

    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        Credential credential = null;
        try {
            InputStream in = GoogleDriveOperations.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("GoogleDriveOperations -> getCredentials -> Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            System.out.println("GoogleDriveOperations -> getCredentials -> clientID: " + flow.getClientId());

        } catch (FileNotFoundException ex) {
            Logger.getLogger(GoogleDriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return credential;
    }

    // nalazi fajl pass.bin na GoogleDrive-u i upisuje u example.bin
    public final File searchFile() throws IOException {

        List<File> files = new ArrayList<>();
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType='application/octet-stream'")
                    .setQ("fullText contains 'pass.bin'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (File file : result.getFiles()) {
                System.out.printf("GoogleDriveOperations -> searchFile -> Found file: %s (%s)\n", file.getName(), file.getId());
            }
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        if (files.isEmpty()) {
            System.out.println("GoogleDriveOperations -> searchFile -> No pass.bin ");
            return null;
        }
        else {
            return files.get(0);
        }
    }

    // pravi novi fajl na GoogleDrive-u pomocu example.bin fajla na lokalu (koji isto pravimo)
    public final File makeNewFile() throws IOException {
        
        java.io.File myObj1 = new java.io.File("../app/src/main/resources/example.bin");
        java.io.File myObj2 = new java.io.File("../app/src/main/resources/example.json");

        boolean myObj1Cond = myObj1.createNewFile();
        boolean myObj2Cond = myObj2.createNewFile();

        if (myObj1Cond && myObj2Cond) {
            System.out.println("GoogleDriveOperations -> makeNewFile -> Files created: " + myObj1.getName());
        } else {
            System.err.println("GoogleDriveOperations -> makeNewFile -> GRESKA ");
        }

        File fileMetadata = new File();
        fileMetadata.setName("pass.bin");

        FileContent mediaContent = new FileContent("application/octet-stream", myObj1);
        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        System.out.println("GoogleDriveOperations -> makeNewFile -> novi fajl na klaudu, File ID: " + file.getId());
        return file;
    }

    // example.bin fajl upisuje u pass.bin na klaudu
    public String updateFile() throws IOException {
        // kriptovanje: od .json fajla pravimo .bin fajl
        AESalgorithm.encryptFile(this.password, this.salt);
        // .bin fajl saljemo na GoogleCloud
        java.io.File filePath = new java.io.File("../app/src/main/resources/example.bin");
        FileContent mediaContent = new FileContent("application/octet-stream", filePath);
        try {
            Drive.Files.Update request = service.files().update(myFile.getId(), new File(), mediaContent)
                    .setFields("id");
            request.getMediaHttpUploader().setDirectUploadEnabled(true);
            File file = request.execute();
            System.out.println("GoogleDriveOperations -> updateFile -> azuriran fajl na klaudu (sifrovan), File ID: " + file.getId());
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleDriveOperations -> updateFile -> Unable to upload file: " + e.getDetails());
        }
        return "";
    }

    //myFile(services.drive.model.File, pass.bin) fajl sa klauda upisuje u example.bin (io.File)
    public final void downloadFile() {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            service.files().get(this.myFile.getId()).executeMediaAndDownloadTo(outputStream);
            ByteArrayOutputStream outputStreamNew = (ByteArrayOutputStream) outputStream;

//            java.io.File myObj1 = new java.io.File("../app/src/main/resources/example.bin");
//            myObj1.createNewFile();

            try ( FileWriter myWriter = new FileWriter("../app/src/main/resources/example.bin")) {
                myWriter.write(outputStreamNew.toString());
                myWriter.close();

                System.out.println("GoogleDriveOperations -> downloadFile -> preuzet fajl (dekriptovan)");
            }
        } catch (IOException ex) {
            Logger.getLogger(GoogleDriveOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isLogged() {
        return logged;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void updatePassword(String password) {
        this.password = password;
        // dekriptovanje
        if (this.logged) {
            this.goodPassword = AESalgorithm.decryptFile(this.password, this.salt);
        }
    }

    public boolean isGoodPassword() {
        return goodPassword;
    }

    public static void main(String args[]) {

        boolean a = true;
        boolean b = false;
        String salt = "1GB6YA6RE6wUG8F3x9Y8rpi_VapoiDFLb";

        //if (b) {
            AESalgorithm.encryptFile("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", salt);
        //} else {
            AESalgorithm.decryptFile("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", salt);
        //}

    }

}
