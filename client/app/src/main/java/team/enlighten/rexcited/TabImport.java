package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by lzn on 2017-05-06.
 */

public class TabImport extends Fragment {
    Button btnText;
    Button btnOcr;
    public String type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_import_file, container, false);

        btnText = (Button) view.findViewById(R.id.btn_import_by_text);
        btnOcr = (Button) view.findViewById(R.id.btn_import_by_ocr);
        if (type.equals("article"))
            btnOcr.setVisibility(View.VISIBLE);
        else
            btnOcr.setVisibility(View.INVISIBLE);

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditFileActivity.class);
                startActivity(intent);
            }
        });
        btnOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OCRActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
