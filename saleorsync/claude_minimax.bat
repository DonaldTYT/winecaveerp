@echo off
:: 1. Force the terminal to route to MiniMax's Anthropic conversion bridge
set ANTHROPIC_BASE_URL=https://api.minimaxi.com/anthropic

:: 2. Input your secret MiniMax token
set ANTHROPIC_AUTH_TOKEN=sk-cp-3Xey16W6vSZYt41dk_nmHswyvOaDRUJuj_LdeDC_PlbOkavwvQPJj9i9P98kj2vgN-8GxpQw2M44ozqCJ-sEfywgTO8zNuUUTaa3Xnf0NGAGDKPOpAo5Cak

:: 3. Wipe out your real Claude key so the terminal doesn't authenticate with Anthropic
set ANTHROPIC_API_KEY=

:: 4. Force the terminal to ask for the MiniMax model ID
set ANTHROPIC_MODEL=minimax-m2.7
set ANTHROPIC_DEFAULT_SONNET_MODEL=minimax-m2.7
set ANTHROPIC_DEFAULT_OPUS_MODEL=minimax-m2.7

:: 5. Boot the official terminal!
npx @anthropic-ai/claude-code