package com.cloverteam.siagabanjir.db

import android.content.Context
import com.cloverteam.siagabanjir.model.Report
import com.cloverteam.siagabanjir.model.SosNumber
import com.cloverteam.siagabanjir.model.User
import com.google.firebase.database.*

class DatabaseHandler(context: Context) {
    private val BASE_URL = "https://siaga-banjir-6b3e7-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database: DatabaseReference = FirebaseDatabase.getInstance(BASE_URL).reference

    // User related operations
    fun addUser(user: User, callback: (Boolean) -> Unit) {
        val userId = database.child("users").push().key
        user.id = userId ?: ""
        database.child("users").child(user.id).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Pengiriman data berhasil
                    callback(true)
                } else {
                    // Pengiriman data gagal
                    callback(false)
                }
            }
    }


    fun getUser(userId: String, callback: (User?) -> Unit) {
        val query = database.child("users").orderByChild("id").equalTo(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user: User? = null
                for (childSnapshot in snapshot.children) {
                    user = childSnapshot.getValue(User::class.java)
                    break
                }
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun deleteUser(user: User) {
        val userId = user.id
        if (userId.isNotEmpty()) {
            database.child("users").child(userId).removeValue()
        }
    }

    // Update user account details (email, full name, phone number, address)
    fun updateUserAccount(user: User, callback: (Boolean) -> Unit) {
        val userId = user.id
        if (userId.isNotEmpty()) {
            val updatedUser = HashMap<String, Any>()
            updatedUser["email"] = user.email
            updatedUser["namaLengkap"] = user.namaLengkap
            updatedUser["noTelepon"] = user.noTelepon
            updatedUser["alamat"] = user.alamat

            database.child("users").child(userId).updateChildren(updatedUser)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update berhasil
                        callback(true)
                    } else {
                        // Update gagal
                        callback(false)
                    }
                }
        } else {
            // ID pengguna kosong
            callback(false)
        }
    }
    // Update user password
    fun updatePassword(user: User, callback: (Boolean) -> Unit) {
        val userId = user.id
        if (userId.isNotEmpty()) {
            val updatedUserPassword = HashMap<String, Any>()
            updatedUserPassword["password"] = user.password

            database.child("users").child(userId).updateChildren(updatedUserPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update berhasil
                        callback(true)
                    } else {
                        // Update gagal
                        callback(false)
                    }
                }
        } else {
            // ID pengguna kosong
            callback(false)
        }
    }

    fun getAllUsers(callback: (List<User>) -> Unit) {
        val userList = mutableListOf<User>()
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                callback(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    // Report related operations

    fun addReport(report: Report, userId: String, callback: (Boolean) -> Unit) {
        val reportId = database.child("reports").push().key
        report.id = reportId ?: ""
        report.userId = userId
        database.child("reports").child(report.id).setValue(report)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Pengiriman data berhasil
                callback(true)
            } else {
                // Pengiriman data gagal
                callback(false)
            }
        }
    }

    fun getAllReports(callback: (List<Report>) -> Unit) {
        val reportList = mutableListOf<Report>()
        database.child("reports").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }
                callback(reportList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun getReportsByUserId(userId: String, callback: (List<Report>) -> Unit) {
        val reportList = mutableListOf<Report>()
        val query = database.child("reports").orderByChild("userId").equalTo(userId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }

                // Lakukan pengurutan data berdasarkan date secara descending
                reportList.sortByDescending { it.date }

                callback(reportList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun getLatestReportsWithStatus3(callback: (List<Report>) -> Unit) {
        val reportList = mutableListOf<Report>()
        val query = database.child("reports").orderByChild("status").equalTo(3.toDouble())
            .limitToLast(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }
                callback(reportList)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun updateReport(report: Report): Boolean {
        val reportId = report.id
        if (reportId.isNotEmpty()) {
            database.child("reports").child(reportId).setValue(report)
            return true
        }
        return false
    }

    fun deleteReport(report: Report) {
        val reportId = report.id
        if (reportId.isNotEmpty()) {
            database.child("reports").child(reportId).removeValue()
        }
    }

    fun getSosNumber(callback: (List<SosNumber>) -> Unit) {
        val sosNumberList = mutableListOf<SosNumber>()
        database.child("sosnumbers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sosSnapshot in snapshot.children) {
                    val sosNumber = sosSnapshot.getValue(SosNumber::class.java)
                    sosNumber?.let { sosNumberList.add(it) }
                }
                callback(sosNumberList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here if needed
                callback(emptyList())
            }
        })
    }

}