package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

//Bean Validation을 이용하는 클래스
//스프링 MVC는 어떻게 Bean Validator를 사용?
//스프링 부트가 spring-boot-starter-validation 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다.
//때문에 ItemValidator객체 검증기를 만들고 등록하지 않아도됨
//스프링 부트는 자동으로 글로벌 Validator로 등록한다. 빈을 검증해줌, item클래스에 @NotNull같은 애노테이션을 검증
//LocalValidatorFactoryBean 을 글로벌 Validator로 등록한다. 이 Validator는 @NotNull 같은 애노테이션을 보고 검증을 수행한다.
//이렇게 글로벌 Validator가 적용되어 있기 때문에, @Valid ,@Validated 만 적용하면 된다.
//스프링은 @Valid ,@Validated를 보면 너는 검증기를 돌려야겠다 하고 Item클래스에서 검증기를 찾음, 이때 LocalValidatorFactoryBean
//검증 오류가 발생하면, FieldError , ObjectError 를 생성해서 BindingResult 에 담아준다

//ValidationItemControllerV2에서 언급했듯이 직접 글로벌 Validator를 직접 등록하면
// 스프링 부트는 Bean Validator를 글로벌 Validator 로 등록하지 않는다. 따라서 애노테이션 기반의 빈 검증기가 동작하지 않는다

//로그 객체 자동으로 생성해주는
@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    //@ModelAttribute는 Item클래스의 필드들을 모델 파라미터에 담아줌,
    //검증 순서
    //1. @ModelAttribute 각각의 필드에 타입 변환 시도
    //성공하면 다음으로
    //2. 실패하면 typeMismatch 로 FieldError 추가
    //Validator 적용
    //바인딩에 성공한 필드만 Bean Validation 적용
    //BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다.
    //생각해보면 타입 변환에 성공해서 바인딩에 성공한 필드여야 BeanValidation 적용이 의미 있다.
    //(일단 모델 객체에 바인딩 받는 값이 정상으로 들어와야 검증도 의미가 있다.)
    //@ModelAttribute 각각의 필드 타입 변환시도 변환에 성공한 필드만 BeanValidation 적용
    //@Validated 는 스프링 전용 검증 애노테이션이고, @Valid 는 자바 표준 검증 애노테이션이다.
    //둘중아무거나 사용해도 동일하게 작동하지만, @Validated 는 내부에 groups 라는 기능을 포함

    //@Validated(SaveCheck.class)는 @Validated가 먹힐때 SaveCheck조건만 먹는거
    //Item클래스에서 SaveCheck가 붙지 않은 어노테이션 조건은 안먹음
    //@Valid는 이런 기능 없음
    @PostMapping("/add")
    public String addItem2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

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
            return "validation/v3/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "validation/v3/editForm";
    }

    //등록과 수정 요구사항이 다름
    //수정시 요구사항
    //등록시에는 quantity 수량을 최대 9999까지 등록할 수 있지만 수정시에는 수량을 무제한으로 변경할 수 있다.
    //등록시에는 id 에 값이 없어도 되지만, 수정시에는 id 값이 필수
    //수정에도 Bean Validato 적용
    //@Validated(UpdateCheck.class)는 @Validated가 먹힐때 UpdateCheck조건만 먹는거
    //Item클래스에서 UpdateCheck가 붙지 않은 어노테이션 조건은 안먹음
    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //에러 있으면 다시 수정 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);

        return "redirect:/validation/v3/items/{itemId}";
    }

}

