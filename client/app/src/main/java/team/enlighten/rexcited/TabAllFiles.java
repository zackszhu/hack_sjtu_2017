package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team.enlighten.rexcited.article.Article;

/**
 * Created by lzn on 2017-05-06.
 */

public class TabAllFiles extends Fragment {
    ListView list;
    SimpleAdapter adapter;
    List<Map<String, Object>> data;
    public String type = "article";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_all_files, container, false);

        list = (ListView) view.findViewById(R.id.file_list);

        data = new ArrayList<Map<String, Object>>();

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileManager.getInstance().fetchArticle();
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            data.clear();
                            adapter.notifyDataSetChanged();
                        }
                    });
                    return;
                }
                data.clear();
                for (Article article : FileManager.getInstance().articles) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("article", article);
                    map.put("title", article.Title);
                    map.put("desc", article.toString().substring(article.Title.length() + 1));
                    data.add(map);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();

        adapter = new SimpleAdapter(
                view.getContext(),
                data,
                R.layout.layout_file_entry,
                new String[]{
                        "title",
                        "desc"
                },
                new int[]{
                        R.id.file_entry_title,
                        R.id.file_entry_desc
                });
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), PreviewActivity.class);
                Article article = (Article) data.get(position).get("article");
                intent.putExtra("article", article.id);
                startActivity(intent);
            }
        });

        return view;
    }
}
