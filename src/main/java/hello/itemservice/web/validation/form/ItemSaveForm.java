package hello.itemservice.web.validation.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

//ValidationItemControllerV4에서 쓸 폼
//html폼을 그대로 받는, 컨트롤러 레벨까지만 쓰는, 화면과 웹에 특화된 기술
//등록 폼, 등록할때는 id를 입력하는게 아니므로 Item클래스와 달리 제외 가능
//그룹 빼도 됨
@Data
public class ItemSaveForm {

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    @NotNull
    @Max(value = 9999)
    private Integer quantity;
}
