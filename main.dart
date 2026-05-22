import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark(),
      home: const MusicHomePage(),
    );
  }
}

class MusicHomePage extends StatelessWidget {
  const MusicHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xff0D0D0D),

      body: SafeArea(
        child: SingleChildScrollView(

          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,

            children: [

              // ================= HEADER =================

              Stack(
                children: [

                  Container(
                    height: 320,
                    width: double.infinity,

                    decoration: const BoxDecoration(
                      image: DecorationImage(

                        image: NetworkImage(
                          "https://i.imgur.com/8Km9tLL.jpg",
                        ),

                        fit: BoxFit.cover,
                      ),
                    ),
                  ),

                  Positioned(
                    top: 10,
                    left: 10,

                    child: IconButton(
                      onPressed: () {},

                      icon: const Icon(
                        Icons.arrow_back,
                        color: Colors.white,
                      ),
                    ),
                  ),

                  Positioned(
                    top: 10,
                    right: 10,

                    child: IconButton(
                      onPressed: () {},

                      icon: const Icon(
                        Icons.more_vert,
                        color: Colors.pink,
                      ),
                    ),
                  ),

                  const Positioned(
                    bottom: 20,
                    left: 20,

                    child: Text(
                      "Eminem",

                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 35,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 20),

              // ================= POPULAR SONGS =================

              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),

                child: Row(
                  mainAxisAlignment:
                  MainAxisAlignment.spaceBetween,

                  children: [

                    const Text(
                      "Popular Songs",

                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    Row(
                      children: const [

                        Text(
                          "Time",

                          style: TextStyle(
                            color: Colors.grey,
                          ),
                        ),

                        SizedBox(width: 15),

                        Text(
                          "More",

                          style: TextStyle(
                            color: Colors.grey,
                          ),
                        ),
                      ],
                    )
                  ],
                ),
              ),

              const SizedBox(height: 10),

              songItem(
                "1",
                "Without Me",
                "Eminem",
                "4:50",
              ),

              songItem(
                "2",
                "Mocking Bird",
                "Eminem",
                "4:10",
              ),

              songItem(
                "3",
                "The Real Slim Shady",
                "Eminem",
                "4:44",
              ),

              songItem(
                "4",
                "Lose Yourself",
                "Eminem",
                "5:22",
              ),

              songItem(
                "5",
                "Godzilla",
                "Eminem",
                "3:30",
              ),

              const SizedBox(height: 10),

              Center(
                child: ElevatedButton(

                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 40,
                      vertical: 12,
                    ),
                  ),

                  onPressed: () {},

                  child: const Text(
                    "Show All",
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ),

              const SizedBox(height: 25),

              // ================= ARTIST ALBUMS =================

              sectionTitle("Artist Albums"),

              albumList(),

              // ================= SINGLE SONGS =================

              sectionTitle("Single Songs"),

              albumList(),

              // ================= FANS =================

              sectionTitle("Eminem Fans Also Listen To"),

              circleArtistList(),

              // ================= MIX =================

              sectionTitle("Top Mix's"),

              albumList(),

              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }
}

// ================= SONG ITEM =================

Widget songItem(
    String number,
    String title,
    String artist,
    String time,
    ) {

  return Padding(
    padding: const EdgeInsets.symmetric(
      horizontal: 16,
      vertical: 8,
    ),

    child: Row(
      children: [

        Text(
          number,

          style: const TextStyle(
            color: Colors.white,
            fontSize: 18,
          ),
        ),

        const SizedBox(width: 10),

        ClipRRect(
          borderRadius: BorderRadius.circular(10),

          child: Image.network(
            "https://i.imgur.com/8Km9tLL.jpg",

            width: 55,
            height: 55,

            fit: BoxFit.cover,
          ),
        ),

        const SizedBox(width: 12),

        Expanded(
          child: Column(
            crossAxisAlignment:
            CrossAxisAlignment.start,

            children: [

              Text(
                title,

                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                ),
              ),

              Text(
                artist,

                style: const TextStyle(
                  color: Colors.grey,
                ),
              ),
            ],
          ),
        ),

        Text(
          time,

          style: const TextStyle(
            color: Colors.white,
          ),
        ),

        const SizedBox(width: 15),

        const Icon(
          Icons.more_vert,
          color: Colors.grey,
        ),
      ],
    ),
  );
}

// ================= TITLE =================

Widget sectionTitle(String title) {

  return Padding(
    padding: const EdgeInsets.symmetric(
      horizontal: 16,
      vertical: 10,
    ),

    child: Row(
      mainAxisAlignment:
      MainAxisAlignment.spaceBetween,

      children: [

        Text(
          title,

          style: const TextStyle(
            color: Colors.white,
            fontSize: 22,
            fontWeight: FontWeight.bold,
          ),
        ),

        const Text(
          "View All",

          style: TextStyle(
            color: Colors.blue,
          ),
        ),
      ],
    ),
  );
}

// ================= ALBUM LIST =================

Widget albumList() {

  return SizedBox(
    height: 190,

    child: ListView.builder(

      scrollDirection: Axis.horizontal,

      itemCount: 5,

      itemBuilder: (context, index) {

        return Padding(
          padding: const EdgeInsets.only(left: 16),

          child: Column(
            crossAxisAlignment:
            CrossAxisAlignment.start,

            children: [

              ClipRRect(
                borderRadius: BorderRadius.circular(15),

                child: Image.network(
                  "https://i.imgur.com/8Km9tLL.jpg",

                  width: 130,
                  height: 130,

                  fit: BoxFit.cover,
                ),
              ),

              const SizedBox(height: 6),

              const Text(
                "Album Name",

                style: TextStyle(
                  color: Colors.white,
                ),
              ),

              const Text(
                "2025",

                style: TextStyle(
                  color: Colors.grey,
                ),
              ),
            ],
          ),
        );
      },
    ),
  );
}

// ================= CIRCLE ARTIST =================

Widget circleArtistList() {

  return SizedBox(
    height: 120,

    child: ListView.builder(

      scrollDirection: Axis.horizontal,

      itemCount: 5,

      itemBuilder: (context, index) {

        return Padding(
          padding: const EdgeInsets.only(left: 16),

          child: Column(
            children: [

              const CircleAvatar(
                radius: 35,

                backgroundImage: NetworkImage(
                  "https://i.imgur.com/8Km9tLL.jpg",
                ),
              ),

              const SizedBox(height: 8),

              const Text(
                "Artist",

                style: TextStyle(
                  color: Colors.white,
                ),
              ),
            ],
          ),
        );
      },
    ),
  );
}