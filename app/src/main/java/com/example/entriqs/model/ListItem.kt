package com.example.entriqs.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Staff(val name: String, val status: String)

data class Security(val name: String, val status: String)

data class Visitor(
    val visitorId: String = UUID.randomUUID().toString(),
    val name: String,
    val checkInTime: String,
    var checkOutTime: String? = null,
    var status: String = "Checked In",
    val photoUri: String? = null,
    val phoneNumber: String = "",
    val purpose: String = "",
    val destination: String = "",
    val idNumber: String = "",
    val isEdited: Boolean = false,
    val editedFields: Set<String> = emptySet(),
    var isVerified: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        visitorId = parcel.readString() ?: UUID.randomUUID().toString(),
        name = parcel.readString() ?: "Unknown Visitor",
        checkInTime = parcel.readString() ?: "Unknown",
        checkOutTime = parcel.readString(),
        status = parcel.readString() ?: "Checked In",
        photoUri = parcel.readString(),
        phoneNumber = parcel.readString() ?: "",
        purpose = parcel.readString() ?: "",
        destination = parcel.readString() ?: "",
        idNumber = parcel.readString() ?: "",
        isEdited = parcel.readByte() != 0.toByte(),
        editedFields = parcel.createStringArray()?.toSet() ?: emptySet(),
        isVerified = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(visitorId)
        parcel.writeString(name)
        parcel.writeString(checkInTime)
        parcel.writeString(checkOutTime)
        parcel.writeString(status)
        parcel.writeString(photoUri)
        parcel.writeString(phoneNumber)
        parcel.writeString(purpose)
        parcel.writeString(destination)
        parcel.writeString(idNumber)
        parcel.writeByte(if (isEdited) 1 else 0)
        parcel.writeStringArray(editedFields.toTypedArray())
        parcel.writeByte(if (isVerified) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Visitor> {
        override fun createFromParcel(parcel: Parcel): Visitor = Visitor(parcel)
        override fun newArray(size: Int): Array<Visitor?> = arrayOfNulls(size)

        fun fromString(visitorString: String): Visitor? {
            val parts = visitorString.split("|")
            return if (parts.size >= 13) {
                Visitor(
                    visitorId = parts[0],
                    name = parts[1],
                    checkInTime = parts[2],
                    checkOutTime = parts[3].takeIf { it != "null" },
                    status = parts[4],
                    photoUri = parts[5].takeIf { it != "null" },
                    phoneNumber = parts[6],
                    purpose = parts[7],
                    destination = parts[8],
                    idNumber = parts[9],
                    isEdited = parts[10].toBoolean(),
                    editedFields = parts[11].split(",").toSet(),
                    isVerified = parts[12].toBoolean()
                )
            } else {
                null
            }
        }
    }

    override fun toString(): String {
        return "$visitorId|$name|$checkInTime|$checkOutTime|$status|$photoUri|$phoneNumber|$purpose|$destination|$idNumber|$isEdited|${editedFields.joinToString(",")}|$isVerified"
    }

    fun getCheckInTimeAsLong(): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = sdf.parse(checkInTime)
            date?.time ?: 0L
        } catch (e: Exception) {
            Log.e("Visitor", "Error parsing checkInTime=$checkInTime for visitor $name", e)
            0L
        }
    }

    fun isValid(): Boolean {
        val isValid = visitorId.isNotEmpty() && name.isNotEmpty() && checkInTime.isNotEmpty()
        if (!isValid) {
            Log.w("Visitor", "Invalid visitor: visitorId=$visitorId, name=$name, checkInTime=$checkInTime")
        }
        return isValid
    }
}

data class Role(
    val roleId: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    var status: String,
    val type: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        roleId = parcel.readString() ?: UUID.randomUUID().toString(),
        name = parcel.readString() ?: "",
        phone = parcel.readString() ?: "",
        email = parcel.readString() ?: "",
        password = parcel.readString() ?: "",
        status = parcel.readString() ?: "",
        type = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roleId)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(status)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Role> {
        override fun createFromParcel(parcel: Parcel): Role = Role(parcel)
        override fun newArray(size: Int): Array<Role?> = arrayOfNulls(size)
    }

    fun isValid(): Boolean {
        return roleId.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty() && status.isNotEmpty() && type.isNotEmpty()
    }
}

// New Incident data class
data class Incident(
    val incidentId: String = UUID.randomUUID().toString(),
    val dateTime: String,
    val location: String,
    val description: String,
    val type: String, // e.g., Theft, Vandalism, Unauthorized Access
    val partiesInvolved: String,
    val actionsTaken: String,
    val reportedBy: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        incidentId = parcel.readString() ?: UUID.randomUUID().toString(),
        dateTime = parcel.readString() ?: "",
        location = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        type = parcel.readString() ?: "",
        partiesInvolved = parcel.readString() ?: "",
        actionsTaken = parcel.readString() ?: "",
        reportedBy = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(incidentId)
        parcel.writeString(dateTime)
        parcel.writeString(location)
        parcel.writeString(description)
        parcel.writeString(type)
        parcel.writeString(partiesInvolved)
        parcel.writeString(actionsTaken)
        parcel.writeString(reportedBy)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Incident> {
        override fun createFromParcel(parcel: Parcel): Incident = Incident(parcel)
        override fun newArray(size: Int): Array<Incident?> = arrayOfNulls(size)

        fun fromString(incidentString: String): Incident? {
            val parts = incidentString.split("|")
            return if (parts.size >= 8) {
                Incident(
                    incidentId = parts[0],
                    dateTime = parts[1],
                    location = parts[2],
                    description = parts[3],
                    type = parts[4],
                    partiesInvolved = parts[5],
                    actionsTaken = parts[6],
                    reportedBy = parts[7]
                )
            } else {
                null
            }
        }
    }

    override fun toString(): String {
        return "$incidentId|$dateTime|$location|$description|$type|$partiesInvolved|$actionsTaken|$reportedBy"
    }

    fun isValid(): Boolean {
        val isValid = incidentId.isNotEmpty() && dateTime.isNotEmpty() && location.isNotEmpty() &&
                description.isNotEmpty() && type.isNotEmpty() && reportedBy.isNotEmpty()
        if (!isValid) {
            Log.w("Incident", "Invalid incident: incidentId=$incidentId, dateTime=$dateTime, location=$location")
        }
        return isValid
    }
}

sealed class ListItem {
    data class StaffItem(val staff: Staff) : ListItem()
    data class SecurityItem(val security: Security) : ListItem()
    data class VisitorItem(val visitor: Visitor) : ListItem()
    data class RoleItem(val role: Role, val type: String) : ListItem()
    data class IncidentItem(val incident: Incident) : ListItem()
}
// Add this at the end of Visitor.kt, after the existing classes

data class EmergencyContact(
    val name: String,
    val role: String,
    val phone: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        name = parcel.readString() ?: "",
        role = parcel.readString() ?: "",
        phone = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(role)
        parcel.writeString(phone)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EmergencyContact> {
        override fun createFromParcel(parcel: Parcel): EmergencyContact = EmergencyContact(parcel)
        override fun newArray(size: Int): Array<EmergencyContact?> = arrayOfNulls(size)

        fun fromString(contactString: String): EmergencyContact? {
            val parts = contactString.split("|")
            return if (parts.size >= 3) {
                EmergencyContact(
                    name = parts[0],
                    role = parts[1],
                    phone = parts[2]
                )
            } else {
                null
            }
        }
    }

    override fun toString(): String {
        return "$name|$role|$phone"
    }
}