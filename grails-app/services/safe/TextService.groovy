package safe

import grails.gorm.services.Service

@Service(Text)
interface TextService {

    Text get(Serializable id)

    List<Text> list(Map args)

    Long count()

    void delete(Serializable id)

    Text save(Text text)

}