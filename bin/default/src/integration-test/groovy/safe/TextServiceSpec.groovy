package safe

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class TextServiceSpec extends Specification {

    TextService textService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Text(...).save(flush: true, failOnError: true)
        //new Text(...).save(flush: true, failOnError: true)
        //Text text = new Text(...).save(flush: true, failOnError: true)
        //new Text(...).save(flush: true, failOnError: true)
        //new Text(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //text.id
    }

    void "test get"() {
        setupData()

        expect:
        textService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Text> textList = textService.list(max: 2, offset: 2)

        then:
        textList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        textService.count() == 5
    }

    void "test delete"() {
        Long textId = setupData()

        expect:
        textService.count() == 5

        when:
        textService.delete(textId)
        sessionFactory.currentSession.flush()

        then:
        textService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Text text = new Text()
        textService.save(text)

        then:
        text.id != null
    }
}
