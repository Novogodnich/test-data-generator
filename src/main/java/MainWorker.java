import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class MainWorker extends Thread{
    private String output;
    private final @NonNull String format;
    private final @NonNull int avglen;

    public String generate(@NonNull String format, int avgLength) {
        if (avgLength < 3) {
            throw new IllegalArgumentException("Average length must be 3 or greater");
        }

        String[] constant = format.split("GEN", -1);
        int count = constant.length - 1;

        List<String> generated = new ArrayList<>(count);
        String characters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJJKLKZXCVBNM";

        for (int i = 0; i < count; i++) {
            int tmp = Shared.random.nextInt(0, 4);
            int length = (tmp < 3) ? (avgLength - tmp) : (avgLength + tmp);

            StringBuilder sb = new StringBuilder(length);
            for (int y = 0; y < length; y++) {
                int index = Shared.random.nextInt(characters.length());
                sb.append(characters.charAt(index));
            }
            generated.add(sb.toString());
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < constant.length; i++) {
            result.append(constant[i]);
            if (i < count) {
                result.append(generated.get(i));
            }
        }

        return result.toString();
    }

    public void run() {
        output = generate(format, avglen);
        System.out.println(output);
    }
}
