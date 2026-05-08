package com.lifeplus.healthcare.util

object DataConstants {
    /** All 64 Bangladesh districts, sorted alphabetically. No placeholder. */
    val districts = listOf(
        "Dhaka", "Chattogram", "Sylhet", "Rajshahi", "Khulna", "Barishal", "Rangpur", "Mymensingh",
        "Gazipur", "Narayanganj", "Cumilla", "Brahmanbaria", "Feni", "Noakhali", "Chandpur", "Lakshmipur",
        "Cox's Bazar", "Bandarban", "Rangamati", "Khagrachhari", "Tangail", "Narsingdi", "Manikganj",
        "Munshiganj", "Faridpur", "Madaripur", "Gopalganj", "Rajbari", "Shariatpur", "Kishoreganj",
        "Netrokona", "Sherpur", "Jamalpur", "Bogura", "Joypurhat", "Naogaon", "Natore", "Pabna",
        "Sirajganj", "Chapai Nawabganj", "Bagerhat", "Chuadanga", "Jashore", "Jhenaidah", "Kushtia",
        "Magura", "Meherpur", "Narail", "Satkhira", "Bhola", "Jhalokathi", "Patuakhali", "Pirojpur",
        "Barguna", "Dinajpur", "Gaibandha", "Kurigram", "Lalmonirhat", "Nilphamari", "Panchagarh",
        "Thakurgaon", "Habiganj", "Moulvibazar", "Sunamganj"
    ).sorted()

    /** Districts with a "Select District" placeholder as first item — for use with DropdownField. */
    val districtsWithPlaceholder = listOf("Select District") + districts

    /** User-friendly blood group labels (A+, A-, …). */
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    /** Blood groups with placeholder — for use with DropdownField. */
    val bloodGroupsWithPlaceholder = listOf("Select Blood Group") + bloodGroups

    /** Internal API format blood groups (A_POS, A_NEG, …). */
    val bloodGroupsFull = listOf("A_POS", "A_NEG", "B_POS", "B_NEG", "O_POS", "O_NEG", "AB_POS", "AB_NEG")

    val emergencyTypes = listOf("Heart Attack", "Accident", "Breathing Issue", "Pregnancy", "Other")

    /** Emergency types with placeholder — for use with DropdownField. */
    val emergencyTypesWithPlaceholder = listOf("Select Emergency Type") + emergencyTypes
}
