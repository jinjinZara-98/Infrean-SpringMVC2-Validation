package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
//final이 붙은 필드 자동으로 생성자 생성
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;

    //검증기
    //ItemValidator클래스 @Component달아줘서 스프링 빈에 등록됨, 자동 의존 주입
    private final ItemValidator itemValidator;

    //컨트롤러인 ValidationItemControllerV2가 호출될 때마다 불려짐
    //밑에 어떤 메서드가 호출되든 검증기를 하나 넣어두는, 항상 검증기를 적용할 수 있음
    //WebDataBinder는 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함
    //WebDataBinder에 검증기를 추가하면 해당 컨트롤러에서는 검증기를 자동으로 적용할 수 있다
    //WebMvcConfigurer객체는 요청이 올때마다 새로 만들어짐
    //@InitBinder은 해당 컨트롤러에만 영향
    //모든 컨트롤러가 호출될때마다 불려지게 하는 글로벌 설정하려면 Validator를 스프링클래스에 상속하고
    //@Override
    // public Validator getValidator() {
    // return new ItemValidator();
    // } 메인클래스에 추가
    //대신 이 코드 지우고, @Validated가 붙어있는 메서드가 존재해야함
    //글로벌 설정을 하면 다음에 설명할 BeanValidator가 자동 등록되지 않는다.
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        //검증기 등록 코드
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //아이템에 바인딩된 결과 bindingResult, 잘 안 담겨 오류가 생기는 item이 담김
    //ValidationItemControllerV1의 errors역할 해줌
    //BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다
    //BindingResult 가 있으면 @ModelAttribute 에 데이터 바인딩 시 오류가 발생해도 컨트롤러가 호출
    //없으면 컨트롤러 호출안하고 오류페이지
    //오류는 2종류, 바인딩 자체가 실패한 오류, 비즈니스와 관련된 검증 오류
//    BindingResult 는 인터페이스이고, Errors 인터페이스를 상속받고 있다.
//    실제 넘어오는 구현체는 BeanPropertyBindingResult 라는 것인데, 둘다 구현하고 있으므로 BindingResult 대신에 Errors 를 사용해도 된다.
//    Errors 인터페이스는 단순한 오류 저장과 조회기능을 제공한다. BindingResult 는 여기에 더해서 추가적인 기능들을 제공

    //BindingResult에 검증 오류를 적용하는 3가지 방법
    //@ModelAttribute 의 객체에 타입 오류 등으로 바인딩이 실패하는 경우 스프링이 FieldError 생성해서
    //BindingResult 에 넣어준다.
    //개발자가 직접 넣어준다.
    //Validator 사용 이것은 뒤에서 설명
//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            //필드 단위의 에러는 FieldError, 객체이름, 필드명, 메시지
            //objectname @ModelAttribute 이름, field 오류가 발생한 필드 이름
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {

            int resultPrice = item.getPrice() * item.getQuantity();

            if (resultPrice < 10000) {
                //특정 필드에 대한 오류가 아닌 객체 자체에서 오류가 생겼기 때문에 ObjectError
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //에러가 존재한다면, 검증에 실패하면 다시 입력 폼으로
        //BindingResult는 자동으로 객체를 모델에 담아줘서 모델에 담는 코드 생략 가능
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);

            return "validation/v2/addForm";
        }

        //애러가 없다면
        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //에러가 나도 입력칸에 데이터가 그대로 있게하는 로직
    //사용자의 입력 데이터가 컨트롤러의 @ModelAttribute 에 바인딩되는 시점에 오류가 발생하면 모델 객체에 사용자 입력 값을 유지하기 어렵다.
    // 예를 들어서 가격에 숫자가 아닌 문자가 입력된다면 가격은 Integer 타입이므로 문자를 보관할 수 있는 방법이 없다.
    // 그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다.
    // 그리고 이렇게 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.
    //FieldError 는 오류 발생시 사용자 입력 값을 저장하는 기능을 제공
    //타임리프의 th:field 는 매우 똑똑하게 동작하는데, 정상 상황에는 모델 객체의 값을 사용하지만 @ModelAttribute Item item,
    //오류가 발생하면 FieldError 에서 보관한 값을 사용해서 값을 출력
    //그래서 v1에선 rejectedvalue값이 없었으므로 출력할게 없어 입력칸이 비어있었던것
//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            //FieldError 3번째 파라미터 rejectedvalue, 거절된 값, 사용자가 입력한 값 가져옴, 숫자입력인데 문자를 입력했다던가
            //오류 발생시 사용자 입력 값을 저장하는 필드
            //FieldError 4번째 파라미터  bindingFailure, 바인딩에 실패했는지 여부, 데이터가 넘어오는게 실패했냐
            //codes : 메시지 코드, arguments : 메시지에서 사용하는 인자
            //이것은 오류 발생시 오류코드로 메시지를 찾기 위해 사용
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                    false, null, null, "상품 이름은 필수 입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,
                    null, null, "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,
                    null ,null, "수량은 최대 9,999 까지 허용합니다."));
        }

        //특정 필드가 아닌 복합 룰 검증
        //ObjectError는 데이터를 보관하는게 아님, 값이 넘어오는게 아니므로
        if (item.getPrice() != null && item.getQuantity() != null) {

            int resultPrice = item.getPrice() * item.getQuantity();

            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",null ,null,
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);

            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증 로직
        //codes : 메시지 코드, arguments : 메시지에서 사용하는 인자
        //이것은 오류 발생시 오류코드로 메시지를 찾기 위해 사용, errors.properties에 코드에 맞는 텍스트를 다 설정해놔서
        //코드 입력하고 파라미터(arguments) 전송하면 그에 맞는 텍스트 반환, 따라서 기본 메시지 설정 안해줘도됨
        //만약 errors.properties에서 코드를 못찾으면 기본메시지 출력

//        codes : required.item.itemName 를 사용해서 메시지 코드를 지정한다. 메시지 코드는 하나가 아니라
//        배열로 여러 값을 전달할 수 있는데, 순서대로 매칭해서 처음 매칭되는 메시지가 사용
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,
                    new String[]{"required.item.itemName"}, null, null));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,
                    new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,
                    new String[]{"max.item.quantity"} ,new Object[]{9999}, null));
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {

            int resultPrice = item.getPrice() * item.getQuantity();

            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"} ,
                        new Object[]{10000, resultPrice}, null));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);

            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //컨트롤러에서 BindingResult 는 검증해야 할 객체인 target 바로 다음에 온다.
        //따라서 BindingResult 는 이미 본인이 검증해야 할 객체인 target 을 알고 있다.

        //객체 이름 출력
        log.info("objectName={}", bindingResult.getObjectName());
        //객체 이름과 그 필드들 출력
        //Item클래스에서 @Data를 붙여줬으므로 target=Item(id=null, itemName=상품, price=100, quantity=1234)
        //이렇게 toString형태로 나옴
        log.info("target={}", bindingResult.getTarget());

        if (!StringUtils.hasText(item.getItemName())) {
            //BindingResult 가 제공하는 rejectValue() , reject() 를 사용하면 FieldError , ObjectError 를
            //직접 생성하지 않고, 깔끔하게 검증 오류를 다룰 수 있다
            //객체이름 안 적어줘도 되고 에러코드도 제약조건만 적어주면 됨
            //new ObjectError("item", new String[] {"required.item", "required"}) 이 역할 해줌
            //rejectValue() , reject() 는 내부에서 MessageCodesResolver 를 사용한다. 여기에서 메시지코드들을 생성
            //FieldError , ObjectError 의 생성자를 보면, 오류 코드를 하나가 아니라 여러 오류 코드를 가질 수 있다.
            //MessageCodesResolver 를 통해서 생성된 순서대로 오류 코드를 보관
            //log.info("errors={} ", bindingResult);로 오류 코드들 다 볼 수 있음
            //타임리프 화면을 렌더링 할 때 th:errors 가 실행된다. 만약 이때 오류가 있다면 생성된 오류 메시지
            //코드를 순서대로 돌아가면서 메시지를 찾는다. 그리고 없으면 디폴트 메시지를 출력
            bindingResult.rejectValue("itemName", "required");
        }

        //이렇게 한줄로도 가능, rejectIfEmptyOrWhitespace는 공백이거나 흰색칸이거나
//        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");


        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 10000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //개발자가 직접 설정한 오류 코드 rejectValue() 를 직접 호출
    //***즁요*** 스프링이 직접 검증 오류에 추가한 경우(주로 타입 정보가 맞지 않음)
    //타입이 맞지 않을때 로그가 출력되면 총 4개의 메시지가 있다
    //typeMismatch.item.price
    //typeMismatch.price
    //typeMismatch.java.lang.Integer
    //typeMismatch
    //스프링은 타입 오류가 발생하면 typeMismatch 라는 오류 코드를 사용하므로 이 코드에다 메시지 넣어주면됨

    //ItemValidator에 검증로직을 따로 만들어 보관하고 여기는 성공로직만

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //ItemValidator객체 만들고 메서드에 파라미터 넣어줌, Target Errors
        itemValidator.validate(item, bindingResult);

        //숫자입력칸에 문자를 넣으면 Item클래스 Integer price, private Integer quantity에 null이 들어가므로
        //타입오류와 필드오류가 같이나므로 타입오류가 나면 바로 입력폼으로 다시 돌려버리기

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);

            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v2/items/{itemId}";
    }

    //@Validated 는 검증기를 실행하라는 애노테이션
    //검증시 @Validated @Valid 둘다 사용가능하다
    //javax.validation.@Valid 를 사용하려면 build.gradle 의존관계 추가가 필요하다.
    //implementation 'org.springframework.boot:spring-boot-starter-validation'
    //@Validated 는 스프링 전용 검증 애노테이션이고, @Valid 는 자바 표준 검증 애노테이션
    //애노테이션이 붙으면 앞서 WebDataBinder 에 등록한 검증기를 찾아서 실행
    //@Validated를 넣어주면 자동으로 Item객체에 검증기 itemValidator가 수행됨, 검증 다 한게 bindingResult에 담겨있음
    //addItemV5랑 똑같이 적용됨
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

