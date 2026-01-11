package com.example.gudangumkm.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.R
import com.example.gudangumkm.data.model.User
import com.example.gudangumkm.data.repository.UserRepository
import com.example.gudangumkm.data.repository.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAdapter(
    private val list: List<User>,
    private val userRepository: UserRepository,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUser: TextView = view.findViewById(R.id.tvUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = list[position]
        holder.tvUser.text = "${user.username} | ${user.phone}"

        holder.itemView.setOnClickListener {
            showOptionDialog(holder.itemView.context, user)
        }
    }

    // ===== DIALOG PILIH UPDATE / DELETE =====
    private fun showOptionDialog(context: Context, user: User) {
        val options = arrayOf("Update", "Delete")

        AlertDialog.Builder(context)
            .setTitle("Pilih Aksi")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUpdateDialog(context, user)
                    1 -> showDeleteDialog(context, user)
                }
            }
            .show()
    }

    // ===== UPDATE USER =====
    private fun showUpdateDialog(context: Context, user: User) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_edit_user, null)

        val etUser = view.findViewById<EditText>(R.id.etUser)
        val etPass = view.findViewById<EditText>(R.id.etPass)
        val etHp = view.findViewById<EditText>(R.id.etHp)

        etUser.setText(user.username)
        etPass.setText(user.password)
        etHp.setText(user.phone)

        AlertDialog.Builder(context)
            .setTitle("Update User")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val updatedUser = user.copy(
                    username = etUser.text.toString(),
                    password = etPass.text.toString(),
                    phone = etHp.text.toString()
                )
                
                CoroutineScope(Dispatchers.IO).launch {
                    when (val result = userRepository.updateUser(updatedUser)) {
                        is Result.Success -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "User diperbarui", Toast.LENGTH_SHORT).show()
                                onDataChanged()
                            }
                        }
                        is Result.Error -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is Result.Loading -> { }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ===== DELETE USER =====
    private fun showDeleteDialog(context: Context, user: User) {
        AlertDialog.Builder(context)
            .setTitle("Hapus User")
            .setMessage("Yakin ingin menghapus user ini?")
            .setPositiveButton("Hapus") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    when (val result = userRepository.deleteUser(user.id)) {
                        is Result.Success -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "User dihapus", Toast.LENGTH_SHORT).show()
                                onDataChanged()
                            }
                        }
                        is Result.Error -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is Result.Loading -> { }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}