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

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    /**
     * WebDataBinder : 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함한다.
     *
     * 컨트롤러가 호출 될 때 마다 (요청이 올 때 마다) WebDataBinder가 생성됨
     * 해당 객체에 검증기를 추가한다.
     * @Validated 애노테이션이 붙으면 WebDataBinder에 등록한 검증기를 찾아 실행한다.
     * 여러 검증기를 등록한 경우 검증기의 supports가 호출되어 매개변수 Class를 통해 구분한다.
     * 이때의 클래스는 @ModelAttribute의 클래스가 넘어간다.
     */
    @InitBinder
    public void init(WebDataBinder dataBinder) {
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

    @PostMapping("/add")
    public String addItemV6(@Validated /* 해당 애노테이션을 통해 검증기가 적용된다 */
                            @ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {
        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { //에러가 비어있지 않으면 (에러가 존재하면)
            log.info("bindingResult = {}", bindingResult);
            /* bindingResult는 자동으로 view에 넘어가는 모델 역할을 한다. */
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {
        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        //검증 로직
        itemValidator.validate(item , bindingResult);

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) { //에러가 비어있지 않으면 (에러가 존재하면)
            log.info("bindingResult = {}", bindingResult);
            /* bindingResult는 자동으로 view에 넘어가는 모델 역할을 한다. */
            return "validation/v2/addForm";
        }

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {
        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        /**
         * StringUtils로 item으로부터 itemName값이 존재하지 않으면 검증하던 로직을
         * ValidationUtils를 활용하여 아래와 같이 한줄로 처리할 수 있다.
         * ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
         * 혹은
         * ValidationUtils.rejectIfEmpty(bindingResult, "itemName", "required");
         * rejectIfEmpty : itemName의 값이 null일 경우 bindingResult를 활용하여 rejectValue생성
         * rejectEmptyOrWhitespace : itemName의 값이 공백이거나 null일경우 bindingResult를 활용하여  rejectValue생성
         */
        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
//            bindingResult.addError(new FieldError("item","itemName", "상품 이름은 필수입니다."));
//            bindingResult.addError(new FieldError("item","itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
            /**
             * 타임리프에서는 정상적인 상황에서는 Model객체의 값을 읽지만, BindingResult를 통해 Error발생시 Model로부터 값을 읽지 않고
             * FieldError에서 rejectedValue에 보관한 값을 사용해서 값을 추출한다.
             * rejectedValue - 거절된값 : item.getItemName()
             * bindingFailure - 데이터 바인딩실패 : false (데이터자체는 제대로 들어왔으므로)
             * code : null
             * arguments : null
             */
//            bindingResult.addError(new FieldError("item","itemName", item.getItemName(), false,new String[]{"required.item.itemName"},null, null));
            bindingResult.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,null, null, "수량은 최대 9,999 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
//                bindingResult.addError(new ObjectError("item", null, null,"가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재값 = " + resultPrice));
//                bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[] {10000, resultPrice}, null));
                bindingResult.reject("totalPriceMin", new Object[] {10000, resultPrice}, null);
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

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,
                            RedirectAttributes redirectAttributes, Model model) {
        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
//            bindingResult.addError(new FieldError("item","itemName", "상품 이름은 필수입니다."));
//            bindingResult.addError(new FieldError("item","itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
            /**
             * 타임리프에서는 정상적인 상황에서는 Model객체의 값을 읽지만, BindingResult를 통해 Error발생시 Model로부터 값을 읽지 않고
             * FieldError에서 rejectedValue에 보관한 값을 사용해서 값을 추출한다.
             * rejectedValue - 거절된값 : item.getItemName()
             * bindingFailure - 데이터 바인딩실패 : false (데이터자체는 제대로 들어왔으므로)
             * code : null
             * arguments : null
             */
            bindingResult.addError(
                    new FieldError("item","itemName", item.getItemName(), false,
                            new String[]{"required.item.itemName"},null, null));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
            bindingResult.addError(
                    new FieldError("item", "price", item.getPrice(), false,
                            new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
//            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,null, null, "수량은 최대 9,999 까지 허용합니다."));
            bindingResult.addError(
                    new FieldError("item", "quantity", item.getQuantity(), false,
                            new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
//                bindingResult.addError(new ObjectError("item", null, null,"가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재값 = " + resultPrice));
                bindingResult.addError(
                        new ObjectError("item", new String[]{"totalPriceMin"},
                                new Object[] {10000, resultPrice}, null));
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

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,
                          RedirectAttributes redirectAttributes, Model model) {
        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(
//                    new FieldError("item","itemName", "상품 이름은 필수입니다."));
                    /**
                     * 타임리프에서는 정상적인 상황에서는 Model객체의 값을 읽지만, BindingResult를 통해 Error발생시 Model로부터 값을 읽지 않고
                     * FieldError에서 rejectedValue에 보관한 값을 사용해서 값을 추출한다.
                     * rejectedValue - 거절된값 : item.getItemName()
                     * bindingFailure - 데이터 바인딩실패 : false (데이터자체는 제대로 들어왔으므로)
                     * code : null
                     * arguments : null
                     */
                    new FieldError("item","itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원 에서 1,000,000원 까지 허용합니다."));
            bindingResult.addError(
                    new FieldError("item", "price", item.getPrice(), false,
                            null, null, "가격은 1,000원 에서 1,000,000원 까지 허용합니다.")
            );
        }
        if (item.getQuantity() == null || item.getQuantity() > 9999) {
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,
                    null, null, "수량은 최대 9,999 까지 허용합니다."));
        }
        // 특정 필드가 아닌 복합 검증
        if (item.getPrice() != null && item.getQuantity() != null) { //금액과 수량 모두 null이 아닌경우
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) { // 10000원 미만일 경우
                bindingResult.addError(
                        /**
                         * codes : null
                         * arguments : null
                         */
                        new ObjectError("item", null, null,
                                "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재값 = " + resultPrice));
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

//    @PostMapping("/add")
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

