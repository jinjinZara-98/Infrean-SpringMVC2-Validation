package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//log남기기위해 log객체
@Slf4j
@Controller
@RequestMapping("/validation/v1/items")
//lombok애노테이션, final이 붙은 필드 생성자를 자동으로 만들어줌
//때문에 의존주입을 위해 final 절대 떼면 안됨
//ItemRepository클래스는 @Repository가 붙어 빈으로 등록되므로 생성자가 하나여서 자동으로 @Autowired 붙음
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);

        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "validation/v1/item";
    }

    //폼을 보여주는
    @GetMapping("/add")
    public String addForm(Model model) {
        //비어있는 item객체를 넘기는 이유는 잘못된 값을 입력했을때 리다이렉트하여 그 값을 다시 보여주게 하기 위해서
        model.addAttribute("item", new Item());

        return "validation/v1/addForm";
    }

    //저장을 하는, 폼에 데이터를 넣으면 item객체를 만들어 데이터가 들어감
    //model.addAttribute("item", item)
    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        //검증 오류 결과를 보관, Map객체 생성
        Map<String, String> errors = new HashMap<>();

        //검증 로직,
        //상품이름칸에 글자가 없다면
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품 이름은 필수입니다.");
        }

        //상품가격이 없거나 1000보다 작거나 1000000보다 크면
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
        }

        //상품수량이 없거나 9999개 넘으면
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
        }

        //특정 필드가 아닌 복합 룰 검증
        //상품가격과 수량이 존재하고 가격 * 수량의 합은 10,000원 이상
        if (item.getPrice() != null && item.getQuantity() != null) {

            int resultPrice = item.getPrice() * item.getQuantity();

            if (resultPrice < 10000) {
                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice);
            }
        }

        //에러가 존재하면, 검증에 실패하면 다시 입력 폼으로
        //만약 검증에서 오류 메시지가 하나라도 있으면 오류 메시지를 출력하기 위해 model errors 를 담고,
        //입력폼이 있는 뷰 템플릿으로 보낸다.
        if (!errors.isEmpty()) {
            //로그 찍어주기, 에러가 여러개면 한줄에 다띄워줌
            log.info("errors = {} ", errors);
            //모델에 Map에러객체 담음
            model.addAttribute("errors", errors);

            return "validation/v1/addForm";
        }

        // Map에러객체 비어있으면
        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);

        return "redirect:/validation/v1/items/{itemId}";
    }

}

