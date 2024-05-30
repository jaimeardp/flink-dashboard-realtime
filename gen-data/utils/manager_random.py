
import random


# List of transaction types
_TRANSACTION_TYPES = [
    "Transfer",          # Simple transfer of MATIC or tokens
    "Approve",           # Granting permission to a smart contract to spend tokens
    "Mint",              # Creating new tokens or NFTs
    "Burn",              # Destroying tokens or NFTs
    "Swap",              # Exchanging one token for another
    "Liquidity Provision",  # Adding tokens to a liquidity pool
    "Yield Farming",     # Participating in yield farming or liquidity mining
    "Vote",              # Participating in governance by voting on proposals
    "Bridge",            # Moving assets between Polygon and another chain
    "Delegate",          # Delegating tokens to a validator for staking
    "Unstake",           # Withdrawing staked tokens
    "Claim Rewards",     # Claiming staking or farming rewards
    "Contract Deployment", # Deploying a new smart contract
    "Contract Interaction" # Interacting with an already deployed smart contract
]


# Function to generate a random crypto symbol
def generate_random_crypto_symbol():
    crypto_symbols = [
        "BTC", "ETH", "XRP", "LTC", "BCH", "EOS", "BNB", "USDT", "ADA", "XLM",
        "TRX", "LINK", "NEO", "IOTA", "DASH", "XMR", "ETC", "ZEC", "XTZ", "DOGE"
    ]
    return random.choice(crypto_symbols)


# Function to select a transaction type
def select_transaction_type():
    # Define weights for each transaction type
    weights = [0.95] + [0.05 / (len(_TRANSACTION_TYPES) - 1)] * (len(_TRANSACTION_TYPES) - 1)
    
    selected_transaction = random.choices(_TRANSACTION_TYPES, weights=weights, k=1)[0]
    return selected_transaction


# Function to generate a random number with spikes
def generate_random_number(mean=10000, stddev=5000, spike_probability=0.01, spike_multiplier=100):
    if random.random() < spike_probability:

        return random.uniform(mean * spike_multiplier, mean * spike_multiplier * 2)
    else:
        return abs(random.gauss(mean, stddev))