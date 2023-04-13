package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz); // 파라미터로 넘어오는 clazz가 Item이 지원되는지 여부
        //item == clazz
        //item == subItem -> 자식클래스 까지 검증
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target; //다운케스팅

        //검증 로직
        /*if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }*/

        // 필수입력
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");

        // 상품 가격 1,000 이상 10,000,000 이하
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        // 상품 수량 9,999이하
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
                errors.reject("totalPriceMin", new Object[] {10000, resultPrice}, null);
            }

        }
    }
}
