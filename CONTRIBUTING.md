Contributing to DND Mapper Automated DND App
Thank you for considering contributing to the DND Mapper Automated DND App! We welcome contributions that help improve the app, extend its features, and enhance its functionality. Please follow the guidelines below to ensure a smooth contribution process.

Project Overview
The DND Mapper Automated DND App is an Android application built using Jetpack Compose and Kotlin that automates the process of enabling Do Not Disturb (DND) mode based on geofence locations. It tracks the device's location in the foreground and background, and when the user enters predefined geofence areas, the app automatically switches the device to DND mode. The app is designed to handle background location permissions and optimize battery usage for background tracking.

Core Features:
Geofence-based automation for enabling/disabling DND mode.

Continuous location tracking in both foreground and background.

Ability to manage and update predefined locations.

Permissions management for ACCESS_BACKGROUND_LOCATION and DND_ACCESS.

Future integration of WorkManager to schedule automatic enabling/disabling of DND mode.

How to Contribute
We encourage contributions from everyone, whether it's fixing bugs, adding new features, or improving documentation. If you're new to the project, please review the following guidelines to ensure that your contributions align with the project's goals and structure.

Steps to Contribute:
Fork the Repository:

Click the "Fork" button at the top right of this repository to create a copy of this project under your GitHub account.

Clone Your Fork:

Clone your forked repository to your local machine using the following command:

bash
Copy
Edit
git clone https://github.com/your-username/DND-Mapper-Automated-DND-App.git
Create a New Branch:

Always create a new branch for each new feature or bug fix:

bash
Copy
Edit
git checkout -b your-feature-branch
Make Your Changes:

Make your changes in your branch, ensuring that you adhere to the code style and project structure.

Add tests if necessary.

Update documentation if relevant.

Commit Your Changes:

Once you have made your changes, commit them with a clear and concise message describing the purpose of your changes:

bash
Copy
Edit
git commit -m "Description of the changes you made"
Push Your Changes:

Push your changes to your forked repository:

bash
Copy
Edit
git push origin your-feature-branch
Create a Pull Request:

After pushing, create a pull request from your feature branch to the main branch of the original repository. This will allow the maintainers to review and merge your changes.

Respond to Feedback:

Be prepared to discuss your pull request and address any feedback provided by the maintainers.

Contribution Guidelines
Code Style
Follow Kotlin's coding conventions for readability and consistency.

Use Jetpack Compose guidelines for UI structure and theming.

Ensure that your code is well-documented with clear comments.

Test your changes thoroughly, especially when introducing new features.

Commit Messages
Use concise, descriptive commit messages.

For bug fixes, mention the issue number (e.g., Fix issue #123).

Use the following format for commit messages:

Feature: Add <feature-name>

Bug Fix: Fix <issue-description>

Refactor: Refactor <module-name>

Docs: Update documentation for <topic>

Branching Strategy
Always create a new branch from main for any feature or bug fix.

Avoid committing directly to the main branch. Contributions should go through pull requests for review.

Pull Requests
Ensure that your pull request targets the main branch of the original repository.

Include a detailed description of your changes and why they are necessary.

Provide a summary of any new features or bug fixes that are included in the pull request.

Project Policies
No Direct Push to Main: Direct pushes to the main branch are not allowed. All changes must be made through pull requests and reviewed before merging.

Branch Protection: The main branch is protected to prevent accidental deletion or changes. Only contributors with bypass permissions are allowed to delete or push changes directly to main.

Testing: All contributions must be accompanied by tests (if applicable). This ensures that the app remains stable and that new features don't break existing functionality.

Code Quality: We maintain high standards of code quality. Please make sure your code is clean, modular, and efficient.

Documentation: Contributions must include proper documentation, especially for new features or changes to existing functionality. Update the README.md and any relevant documentation files.

Future Goals
The DND Mapper Automated DND App is a work in progress, and there are several exciting improvements planned for the future:

Integration with WorkManager: We aim to add WorkManager to schedule the automatic start and stop of location tracking and DND mode activation, further optimizing app performance and battery usage.

Geofencing Enhancements: Improving the geofencing capabilities, such as adding support for multiple geofences or more granular location tracking features.

Battery Optimization: Implementing more efficient background location tracking to minimize battery drain while maintaining reliable tracking.

User Interface Improvements: Enhancing the app's user interface to be more intuitive and user-friendly.

Advanced Features: Exploring additional automation features, such as automatically responding to calls or messages based on the DND status.

Code of Conduct
By participating in this project, you agree to uphold the principles outlined in our Code of Conduct, which promotes a welcoming, respectful, and inclusive environment for everyone.

License
This project is licensed under the MIT License - see the LICENSE.md file for details.

