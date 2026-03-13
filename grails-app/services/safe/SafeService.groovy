package safe

// For Text Safe
import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;

// For Content Safety Image Moderation
import com.azure.core.util.BinaryData;
import com.azure.ai.contentsafety.models.ContentSafetyImageData;
import com.azure.ai.contentsafety.models.ImageCategoriesAnalysis;
import com.azure.ai.contentsafety.models.AnalyzeImageOptions;
import com.azure.ai.contentsafety.models.AnalyzeImageResult;

// For Face Detection
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;

// For Grails Promises
import static grails.async.Promises.task
import static grails.async.Promises.waitAll
import grails.async.Promise


import org.springframework.web.multipart.MultipartFile
import grails.validation.ValidationException;
import grails.gorm.transactions.Transactional;

@Transactional
class SafeService {

    def contentSafe
    def faceDetect

    def textSafe(Text text) {
        // println(""); println(""); println("textSafe called with text: " + text.text);
        AnalyzeTextResult response = contentSafe.client.analyzeText(new AnalyzeTextOptions(text.text));

        int severitySum = 0;
        for (TextCategoriesAnalysis result : response.getCategoriesAnalysis()) {
            severitySum += result.getSeverity()
        }
        // println("Total severity: " + severitySum);
        if (severitySum > 0) {
            text.errors.rejectValue("text", "text.unsafe", "Improper text entry. Please modify your text and try again.");
            throw new ValidationException("Text is not safe", text.errors);
        } 
    }
    
    def moderatePhoto(Photo photo) {
        // println(""); println(""); println("moderatePhoto called with photoFile.class: " + photo.photoFile.getClass()); 
        // BinaryData photoData = BinaryData.fromStream(photoFile.getInputStream());
        // For some reason, using fromStream causes the Face Service to throw an error 
        // about invalid image data. Using fromBytes instead works fine. 
        // Also this will save data in memory after reading. But not ideal for large photos.
        
        byte[] photoBytes = photo.photoFile.getBytes();
        BinaryData photoData = BinaryData.fromBytes(photoBytes);

        // Content Safety Image Moderation    
        Promise<AnalyzeImageResult> contentResults = task{ 
            ContentSafetyImageData image = new ContentSafetyImageData();
            image.setContent(photoData);  
            contentSafe.client.analyzeImage(new AnalyzeImageOptions(image)) 
        };

        // Face Detection
        // See https://learn.microsoft.com/en-us/dotnet/api/azure.ai.vision.face.faceclient.detect?view=azure-dotnet-preview#azure-ai-vision-face-faceclient-detect(system-binarydata-azure-ai-vision-face-facedetectionmodel-azure-ai-vision-face-facerecognitionmodel-system-boolean-system-collections-generic-ienumerable((azure-ai-vision-face-faceattributetype))-system-nullable((system-boolean))-system-nullable((system-boolean))-system-nullable((system-int32))-system-threading-cancellationtoken)
        
        Promise<List<FaceDetectionResult>> faceResults = task{ 
            faceDetect.client.detect(
                photoData,
                FaceDetectionModel.DETECTION_03, 
                FaceRecognitionModel.RECOGNITION_04,
                false, // returnFaceId, do not need.
                null, // FaceAttributeTyes, do not need. 
                false, // returnFaceLandmarks, do not need.
                false, // returnFaceAttributes, do not need.
                60); // faceIdTimeToLive in seconds. The shortest time.
            };

        waitAll(contentResults, faceResults);
        
        // Check content safety results
        int severitySum = 0;
        for (ImageCategoriesAnalysis result : contentResults.get().getCategoriesAnalysis()) {
            severitySum += result.getSeverity()
        }
        if (severitySum > 0) {
            throw new ValidationException("photo", "photo.unsafe", "Improper photo. Please use a different photo and try again.");
        }
        // Check face detection results
        if (faceResults.get().size() > 0) {
            photo.errors.rejectValue("photoFile", "photo.unsafe", "Photo contains a face. Please use a different photo and try again.");
            throw new ValidationException("Photo is not safe", photo.errors);
        }

        
    }   
}
