package safe

// For Text Safe
import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;

// For Photo Safe
import com.azure.core.util.BinaryData;
import com.azure.ai.contentsafety.models.ContentSafetyImageData;
import com.azure.ai.contentsafety.models.ImageCategoriesAnalysis;
import com.azure.ai.contentsafety.models.AnalyzeImageOptions;
import com.azure.ai.contentsafety.models.AnalyzeImageResult;


import org.springframework.web.multipart.MultipartFile
import grails.validation.ValidationException;
import grails.gorm.transactions.Transactional;

@Transactional
class SafeService {

    def contentSafe

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
    
    def moderatePhoto(MultipartFile photoFile) {
        ContentSafetyImageData image = new ContentSafetyImageData();
        image.setContent(BinaryData.fromStream(photoFile.getInputStream()));   

        AnalyzeImageResult response = contentSafe.client.analyzeImage(new AnalyzeImageOptions(image));

        int severitySum = 0;
        for (ImageCategoriesAnalysis result : response.getCategoriesAnalysis()) {
            severitySum += result.getSeverity()
        }

        if (severitySum > 0) {
            throw new ValidationException("photo", "photo.unsafe", "Improper photo. Please use a different photo and try again.");
        }
    }   
}
