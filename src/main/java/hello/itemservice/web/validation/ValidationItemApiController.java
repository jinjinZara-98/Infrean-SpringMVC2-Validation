package hello.itemservice.web.validation;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * API로 json 이동할때 Bean Validation 어떻게 사용하는지 설명하는 클래스
 *
 * Bean Validation인 @Valid , @Validated 는 HttpMessageConverter ( @RequestBody )에도 적용할 수 있다.
 * @ModelAttribute 는 HTTP 요청 파라미터(URL 쿼리 스트링, POST Form)를 다룰 때 사용한다.
 * @RequestBody 는 HTTP Body의 데이터를 객체로 변환할 때 사용한다. 주로 API JSON 요청을 다룰 때 사용
 * HttpMessageConverter는 @RequestBody보고 json이네 인식
 * HTTP 요청 파리미터를 처리하는 @ModelAttribute 는 각각의 필드 단위로 세밀하게 적용된다.
 * 그래서 특정 필드에 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리할 수 있었다.
 * HttpMessageConverter 는 @ModelAttribute 와 다르게 각각의 필드 단위로 적용되는 것이 아니라, 전체 객체 단위로 적용
 * 따라서 메시지 컨버터의 작동이 성공해서 Item 객체를 만들어야 @Valid , @Validated 가 적용
 *
 * @ModelAttribute 는 필드 단위로 정교하게 바인딩이 적용된다.
 * 특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩 되고, Validator를 사용한 검증도 적용할 수 있다.
 *
 * @RequestBody 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면 이후
 * 단계 자체가 진행되지 않고 예외가 발생한다. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없다
 * 그래서 정수 값을 적는 price 에 문자열 값을 넣으면 타입이 맞지 않아 객체로 변경하지 못함
 * 이렇게 하면 검증도 진행하지 못하고 컨트롤러 자체도 호출되지 않으며 예외를 던진다
 */
@Slf4j
//@Controller가 아닌 @RestController
//반환값이 논리경로가 아닌 json객체로 바뀌어 화면에 뿌려주는
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    @PostMapping("/add")
    /**
     * @ModelAttribute가 아닌 @RequestBody API json 형식으로 받음
     * @Validated도 넣어 검증기능까지 그렇기에 BindingResult도 같이 옴
     * 타입에 맞지 않는 값 넣으면 컨트롤러 호출 안됨 API는 제이슨통, 어떻게든 객체로 바뀌어야함
     * 바뀌어야 ItemSaveForm 필드에 들어감, 그래야지 검증을 할 수 있음
     * HttpMessageConverter이 ItemSaveForm객체를 만들어야 컨트롤러 호출 가능, 그거조차도 못만들어냄
     * {"itemName":"hello", "price":"A", "quantity": 10} 이 제이슨 한 덩어리를 객체로 바꾸는데 실패

     *
     * API의 경우 3가지 경우를 나누어 생각해야 한다.
     * 성공 요청: 성공
     * 실패 요청: JSON을 객체로 생성하는 것 자체가 실패함
     * 검증 오류 요청: JSON을 객체로 생성하는 것은 성공했고, 검증에서 실패함
     * 검증 오류 요청은 타입변환이 아닌 수량이 넘었을때, 는 HttpMessageConverter 는 성공하지만 검증(Validator)에서 오류가 발생
     * json을 객체로 만드는건 성공해 컨트롤러는 호출되지만 @Validated를 하는데 검증오류가 생겨 BindingResult애 들어감
     */
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {

        log.info("API 컨트롤러 호출");

        //에러가 있으면
        if (bindingResult.hasErrors()) {
            log.info("검증 오류 발생 errors={}", bindingResult);

            /**
             * BindingResult가 갖고있는 모든 에러들을 반환
             * 는 ObjectError 와 FieldError 를 반환
             * 실제 개발할 때는 이 객체들을 그대로 사용하지 말고, 필요한 데이터(거절된 값)만 뽑아서 별도의 API스펙을 정의하고
             * 그에 맞는 객체를 만들어서 반환, 리스트로 반환해 제이슨으로 화면에 보여줌
             * */
            return bindingResult.getAllErrors();
        }

        log.info("성공 로직 실행");
        return form;
    }
}
