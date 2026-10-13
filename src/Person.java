public abstract class Person {

    // Basic information shared by guests and employees
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;
    private String email;

    // Initializes the information for a subclass of Person
    protected Person(String name, int age, String gender, String phoneNumber, String email) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Returns the person's name
    public String getName() {
        return name;
    }

    // Returns the person's age
    public int getAge() {
        return age;
    }

    // Returns the person's gender
    public String getGender() {
        return gender;
    }

    // Returns the person's phone number
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Returns the person's email address
    public String getEmail() {
        return email;
    }

    // Updates the person's phone number
    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Updates the person's email address
    public void updateEmail(String email) {
        this.email = email;
    }

    // Displays the information shared by all people
    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Email: " + email);
    }
}
