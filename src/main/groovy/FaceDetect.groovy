package safe

import com.azure.core.credential.KeyCredential;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;

import io.github.cdimascio.dotenv.Dotenv;

class FaceDetect {

    FaceClient client;
    
    FaceDetect() {
        Dotenv dotenv = Dotenv.load();
        String endpoint = dotenv.get("FACE_ENDPOINT");
        String key = dotenv.get("FACE_APIKEY");

        client = new FaceClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();
    }
}