package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

//Form 전송 객체 분리
//실무에서는 groups 를 잘 사용하지 않는데, 그 이유가 다른 곳에 있다. 바로 등록시 폼에서 전달하는
//데이터가 Item 도메인 객체와 딱 맞지 않기 때문
//소위 "Hello World" 예제에서는 폼에서 전달하는 데이터와 Item 도메인 객체가 딱 맞는다.
//하지만 실무에서는 회원 등록시 회원과 관련된 데이터만 전달받는 것이 아니라, 약관 정보도 추가로 받는 등
//Item과 관계없는 수 많은 부가 데이터가 넘어온다.
//그래서 보통 Item 을 직접 전달받는 것이 아니라, 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의 객체를 만들어서 전달한다.
// 예를 들면 ItemSaveForm 이라는 폼을 전달받는 전용 객체를 만들어서 @ModelAttribute 로 사용한다.
// 이것을 통해 컨트롤러에서 폼 데이터를 전달 받고, 이후 컨트롤러에서 필요한 데이터를 사용해서 Item 을 생성

//폼 데이터 전달에 Item 도메인 객체 사용
//HTML Form -> Item -> Controller -> Item -> Repository
//장점: Item 도메인 객체를 컨트롤러, 리포지토리 까지 직접 전달해서 중간에 Item을 만드는 과정이 없어서 간단하다.
//단점: 간단한 경우에만 적용할 수 있다. 수정시 검증이 중복될 수 있고, groups를 사용해야 한다.
//폼 데이터 전달을 위한 별도의 객체 사용

//HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository
//ItemSaveForm에서  @ModelAttribute을 받는다, Controller에서 필요한 Item생성
//장점: 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있다.
//보통 등록과, 수정용으로 별도의 폼 객체를 만들기 때문에 검증이 중복되지 않는다.
//단점: 폼 데이터를 기반으로 컨트롤러에서 Item 객체를 생성하는 변환 과정이 추가된다.

//수정의 경우 등록과 수정은 완전히 다른 데이터가 넘어온다. 생각해보면 회원 가입시 다루는 데이터와
//수정시 다루는 데이터는 범위에 차이가 있다. 예를 들면 등록시에는 로그인id, 주민번호 등등을 받을 수 있지만,
//수정시에는 이런 부분이 빠진다. 그리고 검증 로직도 많이 달라진다.
//그래서 ItemUpdateForm이라는 별도의 객체로 데이터를 전달받는 것이 좋다.

//Item 도메인 객체를 폼 전달 데이터로 사용하고, 그대로 쭉 넘기면 편리하겠지만, 앞에서 설명한 것과 같이
//실무에서는 Item 의 데이터만 넘어오는 것이 아니라 무수한 추가 데이터가 넘어온다. 그리고 더 나아가서
//Item 을 생성하는데 필요한 추가 데이터를 데이터베이스나 다른 곳에서 찾아와야 할 수도 있다.

//따라서 이렇게 폼 데이터 전달을 위한 별도의 객체를 사용하고, 등록, 수정용 폼 객체를 나누면 등록, 수정이
//완전히 분리되기 때문에 groups 를 적용할 일은 드물다.

//한 페이지에 그러니까 뷰 템플릿 파일을 등록과 수정을 합치는게 좋을지 고민이 될 수 있다. 각각 장단점이
//있으므로 고민하는게 좋지만, 어설프게 합치면 수 많은 분기분(등록일 때, 수정일 때) 때문에 나중에 유지보수에서 고통을 맛본다.
@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());

        return "validation/v4/addForm";
    }

    //ValidationItemControllerV3와 달리 @Validated에 그룹을 넣지 않고
    //파라미터로 받는 Item타입자체를 ItemSaveForm으로 바꿈
    //@ModelAttribute("item")를 해주지 않으면 model.attribute("itemSaveForm", form) 이렇게 되기 때문에
    //데이터를 보내는 이름을 지정, 그래야 타임리프로 꺼낼때 item으로 꺼낼 수 있음, th:object="${item}"
    //ItemSaveForm 필드들에 값이 들오고 모델에 담겨 html에 넘아감
    //어차피 form으로 전송하면 그 안의 input태그 값이  input태그 nmae에 맞는 클래스 객체 필드에 들어가므로
    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        //특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();

            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={} ", bindingResult);

            return "validation/v4/addForm";
        }

        //성공 로직
        //ItemSaveForm객체 form에 들어온 필드값들을 갖고와 Item객체 item에 다 넣음
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {

        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "validation/v4/editForm";
    }

    //맨 처음 상품 편접 화면 보여줄때는 Item객체를 보내고

    //ValidationItemControllerV3와 달리 @Validated에 그룹을 넣지 않고
    //파라미터로 받는 Item타입자체를 ItemUpdateForm으로 바꿈
    //@ModelAttribute("item")를 해주지 않으면 model.attribute("ItemUpdateForm", form) 이렇게 되기 때문에
    //데이터를 보내는 이름을 지정, 그래야 타임리프로 꺼낼때 item으로 꺼낼 수 있음, th:object="${item}"
    //ItemUpdateForm 필드들에 값이 들오고 모델에 담겨 html에 넘아감
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        //특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) {

            int resultPrice = form.getPrice() * form.getQuantity();

            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "validation/v4/editForm";
        }

        //ItemSaveForm객체 form에 들어온 필드값들을 갖고와 Item객체 item에 다 넣음
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

