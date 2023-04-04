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
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        //검증 오류 결과를 보관
        HashMap<Object, Object> errors = new HashMap<>();

        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) { //넘어온 상품명 글자가 없으면
            errors.put("itemName", "상품 이름은 필수입니다.");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) { //금액이 null 혹은 1000미만 혹은 100만원을 초과할경우
            errors.put("price", "가격은 1,000원 에서 1,000,000원 까지 허용합니다.");
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) { // 주문 수량이 null 혹은 9,999이상일 경우
            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재값 = " + resultPrice);
            }
        }
        //검증에 실패하면 다시 입력 폼으로
        if (!errors.isEmpty()) { //에러가 비어있지 않으면 (에러가 존재하면)
            log.info("errors = {}", errors);
            model.addAttribute("errors", errors);
            /**
             * @ModelAttribute에 의해 이미 받아온 값을 다시 넘겨준다!
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

