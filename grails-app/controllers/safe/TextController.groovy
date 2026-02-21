package safe

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class TextController {

    TextService textService
    SafeService safeService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond textService.list(params), model:[textCount: textService.count()]
    }

    def show(Long id) {
        respond textService.get(id)
    }

    def create() {
        respond new Text(params)
    }

    def save(Text text) {
        if (text == null) {
            notFound()
            return
        }

        try {
            safeService.textSafe(text) // Check if text is safe. If not, textService will throw a ValidationException. 
            textService.save(text)  
            respond text.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'text.label', default: 'Text'), text.id])
                redirect text
            }
            '*' { respond text, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond textService.get(id)
    }

    def update(Text text) {
        if (text == null) {
            notFound()
            return
        }

        try {
            textService.save(text)
        } catch (ValidationException e) {
            respond text.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'text.label', default: 'Text'), text.id])
                redirect text
            }
            '*'{ respond text, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        textService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'text.label', default: 'Text'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'text.label', default: 'Text'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
