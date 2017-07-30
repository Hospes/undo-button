package ua.hospes.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ua.hospes.undobutton.UndoButton;
import ua.hospes.undobutton.UndoButtonController;

public class MainActivity extends Activity {
    private UndoButtonController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UndoButton undo = (UndoButton) findViewById(R.id.undo);

        undo.setOnUndoClickListener(v -> Toast.makeText(this, "Undo click", Toast.LENGTH_SHORT).show());
        undo.setOnClickListener(v -> Toast.makeText(this, "Default click", Toast.LENGTH_SHORT).show());

        controller = new UndoButtonController<MyAdapter.MyHolder>(this) {
            @Override
            public UndoButton[] provideUndos(MyAdapter.MyHolder holder) {
                return new UndoButton[]{holder.undoButton};
            }

            @Override
            public boolean defaultTimeSHow() {
                return true;
            }

            @Override
            public int defaultDelay() {
                return 5;
            }
        };
        RecyclerView rv = (RecyclerView) findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.addOnScrollListener(controller);
        rv.setAdapter(new MyAdapter(controller));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.release();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {
        private final UndoButtonController undoButtonController;

        MyAdapter(UndoButtonController undoButtonController) {
            this.undoButtonController = undoButtonController;
        }

        @Override
        public MyAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyAdapter.MyHolder(parent);
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyHolder holder, int position) {
            undoButtonController.onBind(position, holder.undoButton);
        }

        @Override
        public int getItemCount() {
            return 6;
        }

        class MyHolder extends RecyclerView.ViewHolder {
            UndoButton undoButton;

            MyHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));

                undoButton = (UndoButton) itemView.findViewById(R.id.undo);

                undoButton.setOnClickListener(this::initOnItemClickListener);
                undoButton.setOnUndoClickListener(this::initOnItemUndoListener);
                undoButton.setController(undoButtonController);
            }

            void initOnItemClickListener(View view) {
                final int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Toast.makeText(view.getContext(), "Item click: " + position, Toast.LENGTH_SHORT).show();
            }

            void initOnItemUndoListener(View view) {
                final int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Toast.makeText(view.getContext(), "Undo click: " + position, Toast.LENGTH_SHORT).show();
            }
        }
    }
}