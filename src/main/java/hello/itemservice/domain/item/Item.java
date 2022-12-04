package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
//NotNull와 NotBlank는 Bean Validation이 표준적으로 제공하는
//Range는 하이버네이트
//javax.validation 으로 시작하면 특정 구현에 관계없이 제공되는 표준 인터페이스이고,
//org.hibernate.validator로 시작하면 하이버네이트 validator 구현체를 사용할 때만 제공되는 검증기능이다.
//실무에서 대부분 하이버네이트 validator를 사용하므로 자유롭게 사용해도 된다.

@Data
//닌 해당 오브젝트 관련 오류( ObjectError ) 처리, 메시지 설정안하면 기본 메시지 출력
//메시지 코드 ScriptAssert.item, ScriptAssert, 실제 사용하기에는 기능이 약하고 복잡
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000"
//,message="10000원 넘게 입력해주세요.")
public class Item {

    // 현재 구조에서는 수정시 item 의 id 값은 항상 들어있도록 로직이 구성되어 있다. 그래서 검증하지 않아도
    //된다고 생각할 수 있다. 그런데 HTTP 요청은 언제든지 악의적으로 변경해서 요청할 수 있으므로 서버에서
    //항상 검증해야 한다. 예를 들어서 HTTP 요청을 변경해서 item 의 id 값을 삭제하고 요청할 수도 있다.
    //따라서 최종 검증은 서버에서 진행하는 것이 안전

    //검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고, 표준화 한 것이 바로 Bean Validation
    //Bean Validation을 잘 활용하면, 애노테이션 하나로 검증 로직을 매우 편리하게 적용할 수 있다.
    //기술 표준
    //검증 애노테이션과 여러 인터페이스의 모음이다. 마치 JPA가 표준 기술이고 그 구현체로 하이버네이트가 있는 것과 같다.
    //Bean Validation을 구현한 기술중에 일반적으로 사용하는 구현체는 하이버네이트 Validator, 구현체를 바꿔 낄 수 있음

    //오류 코드가 애노테이션 이름으로 등록된다. 마치 typeMismatch 와 유사
    //NotBlank 라는 오류 코드를 기반으로 MessageCodesResolver 를 통해 다양한 메시지 코드가 순서대로 생성
    //메시지

    //수정 요구사항 추가, 추가 요구사항은 해당되지 않음
    //상품 등록할때는 입력에 id입력이 없어  @NotNull할 수가 없음
    //그래서 상풍 등록때 id가 null로 들어가 등록이 안되는 상황 발생
    //등록과 수정 요구사항 충돌 발생 수량제한도 수정에서는 풀려서 충돌

    //행결방법 2가지
    //BeanValidation의 groups 기능을 사용
    //Item을 직접 사용하지 않고, ItemSaveForm, ItemUpdateForm 같은
    //폼 전송을 위한 별도의 모델객체를 만들어서 사용

    //수정 요구사항에만 적용하게 groups = UpdateCheck.class
//    @NotNull(groups = UpdateCheck.class)

    //ValidationItemControllerV4에서 폼을 아예 추가, 수정 2가지로 분류해놔서 어노테이션을 web form안에 클래스들에서 씀
    private Long id;

    //테스트할때 콘솔에 나타나는 메시지 정하기, 홈페이지에도 나타남
    //@NotBlank(message = "공백X")

    //빈값 + 공백만 있는 경우를 허용하지 않는다, 추가 수정 둘다 적용
//    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;


    //값이 null이면 안되고 범위가 1000부터 1000000까지
    //숫자인데 문자가 들어오면 null이?
//    @NotNull
//    @Range(min = 1000, max = 1000000)

    //추가 수정 둘다 적용
//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Range(min = 1000, max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;

    //값이 null이면 안되고 최대 9999
    //@NotNull
    //수정 요구사항 추가
    //@Max(9999)

    //null값은 추가, 수정 둘다, 수량은 등록만 9999제한
//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Max(value = 9999, groups = {SaveCheck.class})
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
