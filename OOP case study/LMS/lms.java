import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

class Book {
    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    public Book(String bookId, String title, String author, String isbn, int totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public boolean borrowBook() {
        if (isAvailable()) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    @Override
    public String toString() {
        return String.format("Book[ID=%s, Title='%s', Author='%s', Available=%d/%d]",
                bookId, title, author, availableCopies, totalCopies);
    }
}

class Transaction {
    private String transactionId;
    private Book book;
    private Member members;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    public Transaction(String transactionId, Book book, Member member) {
        this.transactionId = transactionId;
        this.book = book;
        this.members = member;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(members.getLoanPeriod());
        this.returnDate = null;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public long getDaysOverdue() {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();
        if (checkDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, checkDate);
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Transaction[ID=%s, Book=%s, Borrowed=%s, Due=%s, Status=%s]",
                transactionId, book.getTitle(), borrowDate, dueDate,
                (returnDate == null ? "Active" : "Returned on " + returnDate));
    }
}

abstract class Member {
    protected String memberId;
    protected String name;
    protected String email;
    protected List<Transaction> activeTransactions;
    protected int maxBooks;
    protected int loanPeriod; // in days

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.activeTransactions = new ArrayList<>();
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public int getLoanPeriod() {
        return loanPeriod;
    }

    public List<Transaction> getActiveTransactions() {
        return activeTransactions;
    }

    public boolean canBorrow() {
        return activeTransactions.size() < maxBooks;
    }

    public void addTransaction(Transaction transaction) {
        activeTransactions.add(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        activeTransactions.remove(transaction);
    }

    public abstract double calculateFine(long daysOverdue);

    public abstract String getMemberType();

    @Override
    public String toString() {
        return String.format("%s[ID=%s, Name='%s', Email='%s', Books Borrowed=%d/%d]",
                getMemberType(), memberId, name, email, activeTransactions.size(), maxBooks);
    }
}

class StudentMember extends Member {
    private static final double FINE_PER_DAY = 1.0; // $1 per day

    public StudentMember(String memberId, String name, String email) {
        super(memberId, name, email);
        this.maxBooks = 3;
        this.loanPeriod = 14; // 2 weeks
    }

    @Override
    public double calculateFine(long daysOverdue) {
        return daysOverdue * FINE_PER_DAY;
    }

    @Override
    public String getMemberType() {
        return "Student";
    }
}

class FacultyMember extends Member {
    private static final double FINE_PER_DAY = 0.5; // $0.50 per day

    public FacultyMember(String memberId, String name, String email) {
        super(memberId, name, email);
        this.maxBooks = 5;
        this.loanPeriod = 30; // 1 month
    }

    @Override
    public double calculateFine(long daysOverdue) {
        return daysOverdue * FINE_PER_DAY;
    }

    @Override
    public String getMemberType() {
        return "Faculty";
    }
}

class Library {
    private String libraryName;
    private List<Book> books;
    private List<Member> members;
    private List<Transaction> allTransactions;
    private int transactionCounter;

    public Library(String libraryName) {
        this.libraryName = libraryName;
        this.books = new ArrayList<>();
        this.members = new ArrayList<>();
        this.allTransactions = new ArrayList<>();
        this.transactionCounter = 1;
    }

    public void addBook(Book book) {
        books.add(book);
        System.out.println("Book added: " + book);
    }

    public void registerMember(Member member) {
        members.add(member);
        System.out.println("Member registered: " + member);
    }

    public Book findBook(String bookId) {
        for (Book book : books) {
            if (book.getBookId().equals(bookId)) {
                return book;
            }
        }
        return null;
    }

    public Member findMember(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equals(memberId)) {
                return member;
            }
        }
        return null;
    }

    public boolean issueBook(String memberId, String bookId) {
        Member member = findMember(memberId);
        Book book = findBook(bookId);

        if (member == null) {
            System.out.println("Error: Member not found!");
            return false;
        }

        if (book == null) {
            System.out.println("Error: Book not found!");
            return false;
        }

        if (!member.canBorrow()) {
            System.out.println("Error: Member has reached maximum borrowing limit!");
            return false;
        }

        if (!book.isAvailable()) {
            System.out.println("Error: Book is not available!");
            return false;
        }

        String transactionId = "T" + String.format("%04d", transactionCounter++);
        Transaction transaction = new Transaction(transactionId, book, member);

        book.borrowBook();
        member.addTransaction(transaction);
        allTransactions.add(transaction);

        System.out.println("Book issued successfully!");
        System.out.println("Due date: " + transaction.getDueDate());
        return true;
    }

    public void returnBook(String memberId, String bookId) {
        Member member = findMember(memberId);

        if (member == null) {
            System.out.println("Error: Member not found!");
            return;
        }

        Transaction transaction = null;
        for (Transaction t : member.getActiveTransactions()) {
            if (t.getBook().getBookId().equals(bookId)) {
                transaction = t;
                break;
            }
        }

        if (transaction == null) {
            System.out.println("Error: No active transaction found for this book!");
            return;
        }

        transaction.setReturnDate(LocalDate.now());

        long daysOverdue = transaction.getDaysOverdue();
        double fine = member.calculateFine(daysOverdue);

        transaction.getBook().returnBook();
        member.removeTransaction(transaction);

        System.out.println("Book returned successfully!");
        if (daysOverdue > 0) {
            System.out.printf("Book was %d days overdue.%n", daysOverdue);
            System.out.printf("Fine: $%.2f%n", fine);
        } else {
            System.out.println("Book returned on time. No fine.");
        }
    }

    public void displayStatistics() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("LIBRARY STATISTICS: " + libraryName);
        System.out.println("=".repeat(50));
        System.out.println("Total Books: " + books.size());
        System.out.println("Total Members: " + members.size());
        System.out.println("Total Transactions: " + allTransactions.size());

        int availableBooks = 0;
        for (Book book : books) {
            availableBooks += book.getAvailableCopies();
        }
        System.out.println("Available Book Copies: " + availableBooks);
        System.out.println("=".repeat(50) + "\n");
    }

    public void displayAllBooks() {
        System.out.println("\n--- All Books ---");
        for (Book book : books) {
            System.out.println(book);
        }
    }

    public void displayAllMembers() {
        System.out.println("\n--- All Members ---");
        for (Member member : members) {
            System.out.println(member);
        }
    }
}

// this is main class
class LibraryManagementSystem {
    public static void main(String[] args) {

        Library library = new Library("Central University Library");

        System.out.println("=== LIBRARY MANAGEMENT SYSTEM ===\n");

        System.out.println("--- Adding Books ---");
        library.addBook(new Book("B001", "Introduction to Java", "James Gosling", "ISBN-001", 3));
        library.addBook(new Book("B002", "Data Structures", "Robert Lafore", "ISBN-002", 2));
        library.addBook(new Book("B003", "Design Patterns", "Gang of Four", "ISBN-003", 2));
        library.addBook(new Book("B004", "Clean Code", "Robert Martin", "ISBN-004", 1));

        System.out.println("\n--- Registering Members ---");
        Member student1 = new StudentMember("S001", "Alice Johnson", "alice@university.edu");
        Member student2 = new StudentMember("S002", "Bob Smith", "bob@university.edu");
        Member faculty1 = new FacultyMember("F001", "Dr. Carol White", "carol@university.edu");

        library.registerMember(student1);
        library.registerMember(student2);
        library.registerMember(faculty1);

        library.displayStatistics();
        library.displayAllBooks();
        library.displayAllMembers();

        System.out.println("\n--- Issuing Books ---");
        library.issueBook("S001", "B001"); // Student borrows Java book
        library.issueBook("S001", "B002"); // Student borrows Data Structures
        library.issueBook("F001", "B003"); // Faculty borrows Design Patterns
        library.issueBook("S002", "B004"); // Student borrows Clean Code

        System.out.println("\n--- Attempting to Issue Unavailable Book ---");
        library.issueBook("S002", "B004"); // Should fail - already borrowed

        library.displayStatistics();
        library.displayAllBooks();
        library.displayAllMembers();

        System.out.println("\n--- Returning Books ---");
        library.returnBook("S001", "B001"); // Student returns on time
        library.returnBook("F001", "B003"); // Faculty returns

        System.out.println("\n--- Demonstrating Polymorphism (Fine Calculation) ---");
        System.out.println("Student fine rate: $" + student1.calculateFine(5) + " for 5 days overdue");
        System.out.println("Faculty fine rate: $" + faculty1.calculateFine(5) + " for 5 days overdue");
        System.out.println("(Different rates based on member type - Polymorphism in action!)");

        library.displayStatistics();
        library.displayAllBooks();

        System.out.println("\n=== DEMONSTRATION COMPLETE ===");
    }
}