package com.example.clase9;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.clase9.databinding.ActivityMainBinding;
import com.example.clase9.dtos.Usuario;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    ActivityMainBinding binding;
    ListenerRegistration snapshotListener;

    Query queryListUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.button.setOnClickListener(view -> {
            String nombre = binding.textFieldNombre.getEditText().getText().toString();
            String apellido = binding.textFieldApellido.getEditText().getText().toString();
            String edadStr = binding.textFieldEdad.getEditText().getText().toString();
            String dni = binding.textFieldDni.getEditText().getText().toString();

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEdad(Integer.parseInt(edadStr));

            db.collection("usuarios")
                    .document(dni)
                    .set(usuario)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Usuario grabado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Algo pasó al guardar ", Toast.LENGTH_SHORT).show();
                    });
        });


        binding.btnListarUsuarios.setOnClickListener(view -> {
            String dni = binding.textFieldDni.getEditText().getText().toString();

            if (!dni.isEmpty()) {
                binding.btnListarUsuarios.setEnabled(false);
                db.collection("usuarios")
                        .document(dni)
                        .get()
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    Log.d("msg-test", "DocumentSnapshot data: " + documentSnapshot.getData());

                                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                    Toast.makeText(this, "Nombre: " + usuario.getNombre() + " | apellido: " + usuario.getApellido(), Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                                }
                            }

                            binding.btnListarUsuarios.setEnabled(true);
                        });
            }

        });

        binding.btnTiempoReal.setOnClickListener(view -> {

            String campo = binding.chipNombre.isChecked()?"nombre":
                            binding.chipApellido.isChecked()?"apellido":
                            binding.chipEdad.isChecked()?"edad":"nombre";

            snapshotListener = db.collection("usuarios")
                    .orderBy(campo,
                            Query.Direction.ASCENDING)
                    .limit(3)
                    .addSnapshotListener((collection, error) -> {

                        if (error != null) {
                            Log.w("msg-test", "Listen failed.", error);
                            return;
                        }

                        Log.d("msg-test", "---- Datos en tiempo real ----");
                        for (QueryDocumentSnapshot doc : collection) {
                            Usuario usuario = doc.toObject(Usuario.class);
                            Log.d("msg-test",
                                    "id: " + doc.getId() +
                                    "| Nombre: " + usuario.getNombre() +
                                    " | Apellido: " + usuario.getApellido() +
                                    " | Edad: " + usuario.getEdad());
                        }

                    });
        });


        binding.chipGroupFiltro.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) { // Si un Chip ha sido seleccionado

                if (binding.chipNombre.getId() == checkedId) {
                    Log.d("msg-test-chip", "ChipNombre Checked");
                } else if (binding.chipApellido.getId() == checkedId) {
                    Log.d("msg-test-chip", "ChipApellido Checked");
                } else if (binding.chipEdad.getId() == checkedId) {
                    Log.d("msg-test-chip", "ChipEdad Checked");
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (snapshotListener != null)
            snapshotListener.remove();
    }
}