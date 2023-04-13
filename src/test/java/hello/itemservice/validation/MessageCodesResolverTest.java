package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.FieldError;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.ObjectError;

public class MessageCodesResolverTest {
    /**
     * MessageCodesResolver
     * 검증 오류 코드로 메시지 코드들을 생성하는 인터페이스.
     * DefaultMessageCodesResolver : 구현체
     * ObjectError, FieldError 과 함께 사용
     */
    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    /**
     * resolveMessageCodes()
     * 메시지 코드 추출 테스트 1 - ObjectError
     */
    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        for (String messageCode : messageCodes) {
            System.out.println("messageCode = " + messageCode); // [required.item, required]
        }
//        new ObjectError("item", new String[]{"required.item", "required"});
        Assertions.assertThat(messageCodes).containsExactly("required.item", "required"); // containsExactly : 순서를 포함하여 원소값과 갯수가 정확히 일치
    }
}
