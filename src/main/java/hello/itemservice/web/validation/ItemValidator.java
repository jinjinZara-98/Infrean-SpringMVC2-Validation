package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

//ValidationItemControllerV2 addItemV5에 적용
//검증로직을 따로 만듬, 분리해놓음
//Validator스프링이 제공하는 Validator
@Component
public class ItemValidator implements Validator {

    //검증기를 지원하냐
    //여러 검증기를 등록한다면 그 중에 어떤 검증기가 실행되어야 할지 구분\
    //supports(Item.class) 호출되고, 결과가 true 이므로 ItemValidator 의 validate()가 호출
    @Override
    public boolean supports(Class<?> clazz) {

        //파라미터로 넘어오는 clazz의 타입이 Item타입과 같냐, 자식클래스도 포함
        return Item.class.isAssignableFrom(clazz);
    }

    //검증, supports메서드가 true면 호출?
    @Override
    public void validate(Object target, Errors errors) {
        //타겟을 Item형태로 변환
        Item item = (Item) target;

        //밑에는 ValidationItemControllerV2의 addItemV4 코드

        //errors는 BindingResuult의 부모클래스
        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 10000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        //특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
    }
}
