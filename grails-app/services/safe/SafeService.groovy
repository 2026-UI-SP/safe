package safe

import com.azure.ai.contentsafety.ContentSafetyClient;
import com.azure.ai.contentsafety.models.AnalyzeTextOptions;
import com.azure.ai.contentsafety.models.AnalyzeTextResult;
import com.azure.ai.contentsafety.models.TextCategoriesAnalysis;

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
}
