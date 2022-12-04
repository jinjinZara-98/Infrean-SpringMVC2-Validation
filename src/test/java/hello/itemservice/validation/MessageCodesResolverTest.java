package hello.itemservice.validation;

import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ObjectError;

import static org.assertj.core.api.Assertions.*;

//errors.properties 에서 객체명과 필드명을 조합한 메시지가 있는지 우선 확인하고, 없으면 좀 더 범용적인 메시지를
//선택하도록 추가 개발을 해야겠지만, 범용성 있게 잘 개발해두면, 메시지의 추가 만으로 매우 편리하게 오류 메시지를 관리
//스프링은 MessageCodesResolver 라는 것으로 이러한 기능을 지원

//검증 오류 코드로 메시지 코드들을 생성한다.
//MessageCodesResolver 인터페이스이고 DefaultMessageCodesResolver 는 기본 구현체이다.
//주로 다음과 함께 사용 ObjectError , FieldError
public class MessageCodesResolverTest {

    //에러 코드를 하나 넣으면 여러 개의 값들을 반환, 구현체 DefaultMessageCodesResolver
    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
//    DefaultMessageCodesResolver의 기본 메시지 생성 규칙
//    객체 오류
//    객체 오류의 경우 다음 순서로 2가지 생성
//        1.: code + "." + object name
//        2.: code
//            예) 오류 코드: required, object name: item
//        1.: required.item
//        2.: required
//    필드 오류
//    필드 오류의 경우 다음 순서로 4가지 메시지 코드 생성
//        1.: code + "." + object name + "." + field
//        2.: code + "." + field
//        3.: code + "." + field type
//        4.: code
//    예) 오류 코드: typeMismatch, object name "user", field "age", field type: int
//        1. "typeMismatch.user.age"
//        2. "typeMismatch.age"
//        3. "typeMismatch.int"
//        4. "typeMismatch"

    @Test
    void messageCodesResolverObject() {
        //객체 이름이 item 일 떄 required 라는 에러코드가 포함되어있는 값들 배열로 받기
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        //required.item required 를 포함하는지
        assertThat(messageCodes).containsExactly("required.item", "required");
        //bindingResult.rejectValue("itemName", "required")에서 rejectValue가 messageCodes를 씀
        //그래서 codesResolver호출하고 messageCodes에 담긴 값들을 FIeldError 파라미터에 넣음
        //그리고 값들중 errors.properties에서 우선순위가 높은걸 갖고옴
    }


    @Test
    void messageCodesResolverField() {
        //필드와 필드타입을 추가로 넣음, 들어간 파라미터를 사용해 오류코드를 만드는
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required"
        );
    }

}
