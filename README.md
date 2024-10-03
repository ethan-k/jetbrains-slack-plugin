# JetBrains Deployment Plugin

## Purpose

This plugin enhances the deployment workflow in JetBrains IDEs by integrating Slack notifications and GitHub deployment
status tracking. It's designed to streamline the deployment process and improve team communication.

## Features

- **Slack Integration**: Send deployment notifications directly to a specified Slack channel.
- **GitHub Deployment Status**: Track the status of your deployments directly in the IDE's status bar.
- **Flexible Deployment Options**:
    - Deploy the current branch to staging or production environments.
    - Select specific commits for deployment.
- **Confirmation Dialogs**: Prevent accidental deployments with confirmation prompts.
- **Configurable Settings**: Easily configure Slack tokens, channel names, and other settings via a dedicated tool
  window.

## Plugin Description

<!-- Plugin description -->
The JetBrains Deployment Plugin enhances your development workflow by integrating deployment processes directly into
your IDE. Key features include:

- Slack notifications for deployment events
- Real-time GitHub deployment status tracking
- One-click deployment of current branch or specific commits
- Configurable settings for Slack integration and deployment options
- Safety features like confirmation dialogs for critical actions

This plugin is perfect for teams looking to streamline their deployment process and improve communication around code
releases.
<!-- Plugin description end -->

## Installation

- **Manual Installation**:
  Download the [latest release](https://github.com/ethan-k/jetbrains-slack-plugin/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

1. **Configuration**:

- Open the Slack Plugin tool window or go to <kbd>Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>Slack Plugin
  Settings</kbd>
- Enter your Slack API token and channel name
- Set the number of commits to display in the deployment menu

2. **Deploying**:

- Right-click in the Project view or Editor
- Navigate to the "Slack Deployment" menu
- Choose to deploy the current branch or a specific commit to staging or production

3. **Monitoring**:

- Check the status bar for real-time updates on your deployment status

## Development

This plugin is open for contributions. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request