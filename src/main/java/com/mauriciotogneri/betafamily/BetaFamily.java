package com.mauriciotogneri.betafamily;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BetaFamily
{
    // iOS
    //private static final String TEST_ID = "35199";
    //private static final String TYPE = "ios";

    // Android
    private static final String TEST_ID = "35200";
    private static final String TYPE = "android";

    private static final Integer SHOW = 200;
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded; charset=UTF-8");
    private static final String COOKIE = "_ga=GA1.2.1477955044.1664557961; _fbp=fb.1.1664557961578.644249101; _gid=GA1.2.1939713672.1666354985; PHPSESSID=oqkbibjqkl6m6iqbplhr9bhfe3; beta_session=eyJpdiI6ImRhUXFROWhsWFNkOEhpK01LaEtKS055bGRseGxqbVlBdVwvUFpVSCtoZkxzPSIsInZhbHVlIjoiQkhGclpHdVVPR010OVNwMjJWT1JwUm1YZTRQV2xYUDdpYjFjUlZMU0k0ZVRuWlNpY3N3cG5KMU1iZHpFNzE3QVwvSHBSVUk4cmMxR2kxRGFBRVVadHF3PT0iLCJtYWMiOiIwNzYwZWI1NzQ0ZDI5MzcwYjZhZjEzMTQ3ZDlhZjc0YTc4MzAyZjc1MWYyMjIxYWVkYTJhYjk5OWUyMGI0ZjE2In0%3D";

    public static void main(String[] args) throws Exception
    {
        int skip = 0;
        int testersInvited = 0;

        while (true)
        {
            List<Tester> list = loadTesters(skip);

            for (Tester tester : list)
            {
                boolean success = invite(tester);

                if (success)
                {
                    testersInvited++;
                }
                else
                {
                    System.err.println("Tester not invited: " + new Gson().toJson(tester));
                }
            }

            System.out.println("Current: " + skip + " - Total invited: " + testersInvited);

            skip += SHOW;
        }
    }

    private static List<Tester> loadTesters(int skip) throws Exception
    {
        String form = "testId=" + TEST_ID + "&appTestId=&search=&gender=0&nationality=&device=&osVersion=&ageMin=&ageMax=&show=" + SHOW + "&skip=" + skip + "&type=" + TYPE + "&additionalData%5Badults_in_household%5D%5B%5D=&additionalData%5Badults_in_household%5D%5B%5D=&additionalData%5Bchildren_in_household%5D%5B%5D=&additionalData%5Bchildren_in_household%5D%5B%5D=&additionalData%5Beducation%5D=&additionalData%5Bemployment%5D=&additionalData%5Btesting_experience%5D=&additionalData%5Btechnical_experience%5D=";
        Request request = request("https://betafamily.com/app-tests/members-from-filter", form);

        try (Response response = CLIENT.newCall(request).execute())
        {
            String bodyContent = response.body().string();
            Type listType = new TypeToken<ArrayList<Tester>>()
            {
            }.getType();
            List<Tester> list = new Gson().fromJson(bodyContent, listType);

            if (list.size() == 0)
            {
                System.exit(0);
            }

            return list.stream().filter(Tester::canBeInvited).collect(Collectors.toList());
        }
    }

    private static boolean invite(@NotNull Tester tester) throws Exception
    {
        String form = "memberId=" + tester.id + "&testId=" + TEST_ID + "&deviceId=" + tester.device_id;
        Request request = request("https://betafamily.com/app-tests/invite-internal", form);

        try (Response response = CLIENT.newCall(request).execute())
        {
            if (response.isSuccessful())
            {
                String bodyContent = response.body().string();
                Invitation invitation = new Gson().fromJson(bodyContent, Invitation.class);

                return invitation.success;
            }
            else
            {
                return false;
            }
        }
    }

    private static @NotNull Request request(String url, String form)
    {
        RequestBody body = RequestBody.create(form, FORM);

        return new Request.Builder()
                .url(url)
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Language", "en-US,en;q=0.9,de;q=0.8,es;q=0.7,mt;q=0.6,fr;q=0.5,la;q=0.4,it;q=0.3,pt;q=0.2,th;q=0.1,lv;q=0.1")
                .header("Connection", "keep-alive")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Cookie", COOKIE)
                .header("Origin", "https://betafamily.com")
                .header("Referer", "https://betafamily.com/manage/app/" + TEST_ID)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?1")
                .header("sec-ch-ua-platform", "\"Android\"")
                .post(body)
                .build();
    }
}
