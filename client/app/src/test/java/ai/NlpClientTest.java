package ai;

import org.json.JSONObject;
import org.junit.Test;

import team.enlighten.rexcited.ai.NlpClient;

/**
 * Created by monkey_d_asce on 17-5-6.
 */
public class NlpClientTest {
    @Test
    public void exactMatchSimple() throws Exception {
        JSONObject result = NlpClient.getInstance().exactMatch(
                "I hate banana. but I like rabbit", "I like apple.but I hate rabbit");
        System.out.println(result.toString(1));

    /*
输出解释：
total_num : 表示原句中参与判断的单词个数。
equal_num : 表示匹配的单词个数，那么准确率计算就是 equal_num / total_num
detail: diff,

  {
 "total_num": 7,
 "equal_num": 4,
 "detail": [
  {
   "std": {
    "str": "I",
    "from": 0,
    "to": 0
   },
   "match": 0,
   "user": {
    "str": "I",
    "from": 0,
    "to": 0
   }
  },
  {
   "match": 1,
   "user": {
    "str": "like",
    "from": 1,
    "to": 5
   }
  },
  {
   "match": 1,
   "user": {
    "str": "apple",
    "from": 6,
    "to": 11
   }
  },
  {
   "std": {
    "str": "hate",
    "from": 1,
    "to": 5
   },
   "match": -1
  },
  {
   "std": {
    "str": "banana",
    "from": 6,
    "to": 12
   },
   "match": -1
  },
  {
   "std": {
    "str": "but",
    "from": 13,
    "to": 17
   },
   "match": 0,
   "user": {
    "str": "but",
    "from": 12,
    "to": 15
   }
  },
  {
   "std": {
    "str": "I",
    "from": 18,
    "to": 19
   },
   "match": 0,
   "user": {
    "str": "I",
    "from": 16,
    "to": 17
   }
  },
  {
   "match": 1,
   "user": {
    "str": "hate",
    "from": 18,
    "to": 22
   }
  },
  {
   "std": {
    "str": "like",
    "from": 20,
    "to": 24
   },
   "match": -1
  },
  {
   "std": {
    "str": "rabbit",
    "from": 25,
    "to": 31
   },
   "match": 0,
   "user": {
    "str": "rabbit",
    "from": 23,
    "to": 29
   }
  }
 ]
}

     */
    }


    @Test
    public void fuzzyMatchSimple() throws Exception {
        JSONObject result1 = NlpClient.getInstance().fuzzyMatch("I hate banana. But I like rabbit", "I like apple. And I hate rabbit");
        System.out.println(result1.toString(1));
    }


    @Test
    public void fuzzyMatch() throws Exception {
        String text4 = "Man’s dearest possession might is life. It is given to him but once, and he must live it so as to feel no torturing regrets for wasted years, never know the burning shame of a mean and petty past. So live that, dying, he might say: all my life, all my strength were given to the finest cause in all the world—the fight for the liberation of mankind. ";
        String text5 = "Man’s dearest thing is life. It is given to him but once, and he must live it so as to feel no torturing regrets for wasted years, never know the burning shame of a mean and petty past. So live that, dying, he might say: all my life, all my strength were given to the finest cause in all the world—the fight for the liberation of mankind. ";

        JSONObject result2 = NlpClient.getInstance().fuzzyMatch(text4, text5);
        System.out.println(result2.toString(1));

    }

}