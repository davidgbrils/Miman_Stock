package com.example.gudangumkm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gudangumkm.adapter.UserAdapter
import com.example.gudangumkm.data.repository.UserRepository
import com.example.gudangumkm.data.repository.Result
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity() {
    
    private val userRepository = UserRepository()
    
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_user_list)

        loadData()
    }
    
    private fun loadData() {
        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            when (val result = userRepository.getAllUsers()) {
                is Result.Success -> {
                    rv.adapter = UserAdapter(result.data, userRepository) { loadData() }
                }
                is Result.Error -> {
                    Toast.makeText(this@UserListActivity, "Error: ${result.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> { }
            }
        }
    }
}
