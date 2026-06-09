package com.hmdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
class VoucherOrderControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper mapper;

    @Test
    @DisplayName("login users and write tokens.txt")
    void loginUsersAndWriteTokens() throws Exception {
        List<String> phoneList = userService.lambdaQuery()
                .select(User::getPhone)
                .last("limit 1000")
                .list()
                .stream()
                .map(User::getPhone)
                .collect(Collectors.toList());

        Assertions.assertFalse(phoneList.isEmpty(), "No users found in database");

        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(phoneList.size(), 100));
        List<String> tokenList = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(phoneList.size());

        phoneList.forEach(phone -> executorService.execute(() -> {
            try {
                mockMvc.perform(MockMvcRequestBuilders
                                .post("/user/code")
                                .queryParam("phone", phone))
                        .andExpect(MockMvcResultMatchers.status().isOk());

                String code = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
                Assertions.assertNotNull(code, "Failed to get verification code for phone " + phone);

                LoginFormDTO formDTO = new LoginFormDTO();
                formDTO.setPhone(phone);
                formDTO.setCode(code);

                String tokenJson = mockMvc.perform(MockMvcRequestBuilders
                                .post("/user/login")
                                .content(mapper.writeValueAsString(formDTO))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);

                Result result = mapper.readerFor(Result.class).readValue(tokenJson);
                Assertions.assertTrue(result.getSuccess(), "Failed to get token for phone " + phone);
                tokenList.add(result.getData().toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
            }
        }));

        countDownLatch.await();
        executorService.shutdown();

        Assertions.assertEquals(phoneList.size(), tokenList.size());
        writeToTxt(tokenList, "tokens.txt");
        System.out.println("tokens.txt written, count: " + tokenList.size());
    }

    private static void writeToTxt(List<String> list, String fileName) throws Exception {
        File file = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\" + fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String content : list) {
                bw.write(content);
                bw.newLine();
            }
        }
    }
}
