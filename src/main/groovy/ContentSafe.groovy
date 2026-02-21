package safe

import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.ContentSafetyClientBuilder;
import com.azure.core.credential.KeyCredential;

import io.github.cdimascio.dotenv.Dotenv;

class ContentSafe {

    ContentSafetyClient client;
    
    ContentSafe() {
        println("Initializing Content Safety Client...");

        Dotenv dotenv = Dotenv.load();
        String endpoint = dotenv.get("CONTENT_SAFETY_ENDPOINT");
        String key = dotenv.get("CONTENT_SAFETY_KEY");


        client = new ContentSafetyClientBuilder()
            .credential(new KeyCredential(key))
            .endpoint(endpoint)
            .buildClient();
    }

}