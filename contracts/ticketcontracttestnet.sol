//SPDX-License-Identifier: MIT
pragma solidity ^0.8.29;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract TicketContract is ERC721URIStorage, Ownable {
    uint256 private nextTokenId;
    uint256 public maxTicketsPerEvent;
    mapping(uint256 => bool) public ticketSold;

    event TicketMinted(address indexed recipient, uint256 tokenId, string tokenURI);

    constructor(uint256 _maxTicketsPerEvent) ERC721("EventTickets", "ETK") Ownable(msg.sender) {
        maxTicketsPerEvent = _maxTicketsPerEvent;
    }

    function mintTicket(address recipient, string memory tokenURI) public onlyOwner returns (uint256) {
        require(nextTokenId < maxTicketsPerEvent, "Max ticket limit reached");
        uint256 tokenId = nextTokenId;
        _safeMint(recipient, tokenId);
        _setTokenURI(tokenId, tokenURI);
        ticketSold[tokenId] = false;
        nextTokenId++;
        emit TicketMinted(recipient, tokenId, tokenURI);
        return tokenId;
    }

    function markTicketAsSold(uint256 tokenId) public onlyOwner {
        require(ownerOf(tokenId) != address(0), "Ticket does not exist");
        ticketSold[tokenId] = true;
    }

    function getNextTokenId() public view returns (uint256) {
        return nextTokenId;
    }

    function isTicketSold(uint256 tokenId) public view returns (bool) {
        return ticketSold[tokenId];
    }
}
