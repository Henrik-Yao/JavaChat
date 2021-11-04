import java.util.Random;

public class ChatTest {
    public static void main(String[] args) {
        Random random = new Random();
        String name = "test" + random.nextInt(100);
        new Chat(name);
    }
}
