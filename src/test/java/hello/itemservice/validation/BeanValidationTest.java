package hello.itemservice.validation;

import hello.itemservice.domain.item.Item;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotBlank;
import java.util.Set;

public class BeanValidationTest {

    @Test
    void beanValidation() {
        //공장을 꺼내고
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        //검증기를 꺼낸다
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName(" "); //공백
        item.setPrice(0);
        item.setQuantity(10000);

        //검증기에 상품객체 넣기
        Set<ConstraintViolation<Item>> violations = validator.validate(item);

        //빈값이면 오류가 없는거고, 뭔가 들어있으면 문제가 있는
        //공백일 수 없습니다, 9999 이하여야 합니다, 1000에서 1000000 사이여야 합니다 메시지는 하이버네이트에서 제공해주는
        //그냥 어노테이션만 붙었을때 나타남
        //@NotBlank(message = "공백X") 이렇게 테스트할때 메시지 나타나게 정할 수도 있음, 홈페이지에도 나타남
        for (ConstraintViolation<Item> violation : violations) {
            System.out.println("violation = " + violation);
            System.out.println("violation = " + violation.getMessage());
        }

    }
}
