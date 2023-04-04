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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;

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

    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,
                          /*
                          * BindingResult
                          * BindingResult bindingResult 파라미터의 위치는 @ModelAttribute Item item 다음에 와야 한다.
                          * item의 바인딩 결과가 담긴다. error를 담는 역할을 해준다. (bindingResult는 자동으로 view에 넘어가는 모델 역할을 한다.)
                          * */
                          RedirectAttributes redirectAttributes, Model model) {


        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) { //넘어온 상품명 글자가 없으면
            bindingResult.addError(//FieldError : 필드단위 에러를 저장하는 객체 (오브젝트명, 필드명, 오류메시지)
                    new FieldError("item","itemName", "상품 이름은 필수입니다."));

        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { //금액이 null 혹은 1000미만 혹은 100만원을 초과할경우
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) { // 주문 수량이 null 혹은 9,999이상일 경우
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
            bindingResult.addError(//ObjectError : 특정 필드를 넘어서는 에러를 저장하는 객체(오브젝트명, 오류메시지)
                    new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재값 = " + resultPrice));
            }
        }
        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { //에러가 비어있지 않으면 (에러가 존재하면)
            log.info("bindingResult = {}", bindingResult);
            /**
             * bindingResult는 자동으로 view에 넘어가는 모델 역할을 한다.
             */
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
