# intellij-neo

[![Version](https://img.shields.io/jetbrains/plugin/v/17195.svg)](https://plugins.jetbrains.com/plugin/17195)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17195.svg)](https://plugins.jetbrains.com/plugin/17195)

<!-- Plugin description -->
An Intellij plugin for Neo blockchain.

- Create and manage local private N3 private net instances (uses neo-express)
- Create wallets and transfer assets between wallets
- Deploy and invoke smart contracts
- Explore blocks and transactions

### Prerequisites

This plugin requires .Net v5 (or higher) and neo-express to be installed. Please specify the path fot .Net root
directory and neo express executable in <kbd>
Settings/Preferences</kbd> > <kbd>Tools</kbd> > <kbd>Neo</kbd>

### Usage Instructions

Once enabled, you will be able to see a new tool window titled "Neo" in your IDE. All the functionalities will be
carried using tool window.

#### Creating a private net

Click <kbd>+ Private Net</kbd> button and specify the number of nodes to create a new private net.

#### Functionalities of the tabbed content

- Blocks: Explore blocks and transactions info
- Wallets: Create wallets and transfer assets between them
- Contracts: View deployed contract, deploy new contracts and invoke contracts

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Neo"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/intellij-neo/intellij-neo/releases/latest) and install it manually
  using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Demo

[![Intellij Plugin for Neo](http://img.youtube.com/vi/SZKyt284PnI/0.jpg)](http://www.youtube.com/watch?v=SZKyt284PnI "Intellij Plugin for Neo")

## Donations

Buy me a coffee, Keep this plugin alive.

Neo: `AYj3BcaBdcYMnH6aSGDUvC9jf5CZgJYD3s`

## Special Thanks

This plugin relies on the following components quite heavily. We are super thankful for them ;)

1) Neo-Express - https://github.com/neo-project/neo-express
2) Neow3J Library - https://github.com/neow3j/neow3j