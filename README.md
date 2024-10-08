# Memory Master: Advanced Memory Management for Android Devices

## Overview

**Memory Master** is an innovative Android application designed to optimize memory management on mobile devices. As applications become increasingly resource-intensive, efficient memory management is crucial for maintaining optimal device performance. Memory Master employs advanced memory management concepts to enhance device performance, improve multitasking capabilities, and extend battery life, ultimately providing users with a smoother mobile experience.

## Features

* **Adaptive Memory Allocation:** Learns user behavior over a 7-day period to predict and optimize memory allocation.
* **Real-time Monitoring:** Continuously monitors and optimizes memory usage without user intervention.
* **Intelligent Prediction:** Utilizes historical data and the UsageStatsManager API to predict future memory needs.
* **Background Operation:** Efficiently manages memory using Kotlin coroutines without impacting user experience.
* **User-friendly Interface:** Provides easy-to-understand insights into device memory usage.
* **Battery Life Extension:** Optimizes resource consumption to prolong device battery life.
* **Enhanced Multitasking:** Improves device's ability to run multiple applications simultaneously.

## Getting Started

### Prerequisites

To run this project, you need:

* Android Studio Arctic Fox | 2020.3.1 or later
* Kotlin 1.5.0 or later
* Minimum SDK: Android 5.0 (API level 21)
* Target SDK: Android 11 (API level 30)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/Vishal-BG/MemoryMaster.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the application on your Android device or emulator.

## Usage

After installation:

1. Launch the Memory Master application.
2. Grant necessary permissions when prompted.
3. Allow the app to run in the background for at least 7 days for optimal performance.
4. Use your device normally and let Memory Master optimize your memory usage.
5. Access the app to view memory usage statistics and optimization results.

## Files Structure

* **app/src/main/java/com/example/memorymaster/**
  * **MainActivity.kt**: Main entry point of the application.
  * **MemoryManager.kt**: Core logic for memory management and optimization.
  * **UsageStatsCollector.kt**: Handles collection of app usage statistics.
  * **CoroutineManager.kt**: Manages background tasks using Kotlin coroutines.
* **app/src/main/res/layout/**
  * **activity_main.xml**: Main layout file for the user interface.
* **app/src/main/res/values/**
  * **strings.xml**: Contains all string resources.
  * **styles.xml**: Defines the application's visual styles.

## Key Functions

* **learnUserBehavior():** Analyzes app usage patterns over a 7-day period.
* **predictMemoryNeeds():** Utilizes historical data to forecast future memory requirements.
* **optimizeMemoryAllocation():** Dynamically allocates memory based on predictions and current usage.
* **monitorPerformance():** Tracks and reports on the device's performance metrics.

## Customization

You can modify the learning period, optimization algorithms, and UI elements by adjusting the corresponding parameters in `MemoryManager.kt` and `activity_main.xml`.

## Contributors

* Vishal BG (2341669)
* Sudheer Kumar R (2341662)

## Acknowledgements

Special thanks to Dr. Arokia Paul Rajan R from the Department of Computer Science for guidance and support throughout the development of this project.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For any queries, suggestions, or issues, please open an issue in this repository or contact the contributors directly.

---

**Note**: This project is part of an academic study and is not intended for commercial use without proper authorization. Memory Master is designed to work within the constraints of the Android operating system and does not require root access or system-level modifications.

Feel free to contribute by reporting issues or submitting pull requests to enhance Memory Master!

**Developed by Vishal BG and Sudheer Kumar R**
